package com.mediatek.contacts.aassne;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Aas;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountType.EditField;
import com.android.contacts.common.model.account.AccountType.EditType;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.RawContactDeltaList;
import com.android.contacts.common.model.RawContactModifier;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.R;

import com.google.android.collect.Lists;

import com.mediatek.contacts.simcontact.SlotUtils;
import com.mediatek.contacts.simservice.SimServiceUtils;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.internal.telephony.uicc.AlphaTag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class SimAasEditor {
    private final static String TAG = "SimAasEditor";

    public static final int TYPE_FOR_PHONE_NUMBER = 0;
    public static final int TYPE_FOR_ADDITIONAL_NUMBER = 1;

    public static final int VIEW_UPDATE_NONE = 0;
    public static final int VIEW_UPDATE_HINT = 1;
    public static final int VIEW_UPDATE_VISIBILITY = 2;
    public static final int VIEW_UPDATE_DELETE_EDITOR = 3;
    public static final int VIEW_UPDATE_LABEL = 4;
    public static final int TYPE_OPERATION_AAS = 0;
    public static final int VIEW_TYPE_SUB_KIND_TITLE_ENTRY = 6;

    public static final int OPERATION_CONTACT_INSERT = 1;
    public static final int OPERATION_CONTACT_EDIT = 2;
    public static final int OPERATION_CONTACT_COPY = 3;

    public static final char STRING_PRIMART = 0;
    public static final char STRING_ADDITINAL = 1;

    public static final int SIM_ID_DONT_KNOW_CURR = 10;
    //should be in sync with host app
    private static final int FLAGS_USIM_NUMBER = EditorInfo.TYPE_CLASS_PHONE;
    protected static final int FLAGS_EMAIL = EditorInfo.TYPE_CLASS_TEXT
            | EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
    protected static final int FLAGS_PHONE = EditorInfo.TYPE_CLASS_PHONE;
    private Context mContext = null;
    private ArrayList<Anr> mOldAnrsList = new ArrayList<Anr>();
    private ArrayList<Anr> mAnrsList = new ArrayList<Anr>();
    private ArrayList<Anr> mCopyAnrList = null;
    private Uri mCopyUri = null;
    private int mInsertFlag = 0;

    private static SimAasEditor sOpAasExtension = null;
    private static final String SIM_NUM_PATTERN = "[+]?[[0-9][*#pw,;]]+[[0-9][*#pw,;]]*";
    private static final int SLOT_ID1 = com.android.internal.telephony.PhoneConstants.SIM_ID_1;
    private static final int SLOT_ID2 = com.android.internal.telephony.PhoneConstants.SIM_ID_2;

    public SimAasEditor(Context context) {
        mContext = context;
        sOpAasExtension = this;
    }

    // IP start
    public void setCurrentSubId(int subId) {
        Log.d(TAG, "[setCurrentSubId] subId: = " + subId);
        SimAasSneUtils.setCurrentSubId(subId);
    }

    public void updatePhoneType(int subId, DataKind kind) {
        if (kind.typeList == null) {
            kind.typeList = Lists.newArrayList();
        } else {
            kind.typeList.clear();
        }
        List<AlphaTag> atList = SimAasSneUtils.getAAS(subId);
        final int specificMax = -1;

        kind.typeList.add((new EditType(Anr.TYPE_AAS, R.string.aas_phone_type_none))
                .setSpecificMax(specificMax));
        for (AlphaTag tag : atList) {
            final int recordIndex = tag.getRecordIndex();
            Log.d(TAG, "[updatePhoneType] label=" + tag.getAlphaTag());

            kind.typeList.add((new EditType(Anr.TYPE_AAS, Phone
                    .getTypeLabelResource(Anr.TYPE_AAS))).setSpecificMax(
                    specificMax).setCustomColumn(Aas.buildIndicator(subId, recordIndex)));

        }
        kind.typeList.add((new EditType(Phone.TYPE_CUSTOM, Phone
                .getTypeLabelResource(Phone.TYPE_CUSTOM)))
                .setSpecificMax(specificMax));

        kind.fieldList = Lists.newArrayList();
        kind.fieldList.add(new EditField(Phone.NUMBER, mContext.getResources()
                .getIdentifier("local_phone_account", "string", "com.android.contacts"),
                FLAGS_USIM_NUMBER));
        Log.d(TAG, "[updatePhoneType] subId = " + subId + " specificMax=" + specificMax);
    }

    private void ensureKindExists(RawContactDelta state, String mimeType,
            DataKind kind, int subId) {
        if (state == null) {
            Log.w(TAG, "[ensureKindExists] state is null,return!");
            return;
        }
        if (kind != null) {
            ArrayList<ValuesDelta> values = state.getMimeEntries(mimeType);
            final int slotAnrSize = SlotUtils.getUsimAnrCount(subId);
            /// fix ALPS02795593.remove anr field when not support anr.@{
            removeAnrFieldIfNotSupportAnr(state, values, slotAnrSize);
            /// @}
            if (values != null && values.size() == slotAnrSize + 1) {
                // primary number + slotNumber size
                int anrSize = 0;
                for (ValuesDelta value : values) {
                    Integer isAnr = value.getAsInteger(Data.IS_ADDITIONAL_NUMBER);
                    if (isAnr != null && (isAnr.intValue() == 1)) {
                        anrSize++;
                    }
                }
                Log.d(TAG, "[ensureKindExists] size=" + values.size() + ",slotAnrSize="
                        + slotAnrSize + ",anrSize=" + anrSize);
                if (anrSize < slotAnrSize && values.size() > 1) {
                    for (int i = 1; i < values.size(); i++) {
                        values.get(i).put(Data.IS_ADDITIONAL_NUMBER, 1);
                    }
                }

                return;
            }
            if (values == null || values.isEmpty()) {
                Log.d(TAG, "[ensureKindExists] Empty, insert primary: and anr:" + slotAnrSize);
                // Create child when none exists and valid kind
                final ValuesDelta child = RawContactModifier.insertChild(state, kind);
                if (kind.mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    child.setFromTemplate(true);
                }

                for (int i = 0; i < slotAnrSize; i++) {
                    final ValuesDelta slotChild = RawContactModifier.insertChild(state, kind);
                    slotChild.put(Data.IS_ADDITIONAL_NUMBER, 1);
                }
            } else {
                int pnrSize = 0;
                int anrSize = 0;
                if (values != null) {
                    for (ValuesDelta value : values) {
                        Integer isAnr = value.getAsInteger(Data.IS_ADDITIONAL_NUMBER);
                        if (isAnr != null && (isAnr.intValue() == 1)) {
                            anrSize++;
                        } else {
                            pnrSize++;
                        }
                    }
                }
                Log.d(TAG, "[ensureKindExists] pnrSize=" + pnrSize + ", anrSize=" + anrSize +
                        ",slotAnrSize: " + slotAnrSize);
                if (pnrSize < 1) {
                    // insert a empty primary number if not exists.
                    final ValuesDelta slotChild = RawContactModifier.insertChild(state, kind);
                    slotChild.put(Data.DATA2, 2);
                }
                for (; anrSize < slotAnrSize; anrSize++) {
                    // insert additional numbers if not full.
                    final ValuesDelta slotChild = RawContactModifier.insertChild(state, kind);
                    slotChild.put(Data.IS_ADDITIONAL_NUMBER, 1);
                }
            }
        }
    }

    public boolean ensurePhoneKindForEditor(AccountType type, int subId, RawContactDelta entity) {
        SimAasSneUtils.setCurrentSubId(subId);
        String accountType = entity.getAccountType();

        if (SimAasSneUtils.isUsimOrCsim(accountType)) {
            DataKind dataKind = type.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
            if (dataKind != null) {
                updatePhoneType(subId, dataKind);
            }
            ensureKindExists(entity, Phone.CONTENT_ITEM_TYPE, dataKind, subId);
        }
        return true; // need to check later
    }

    public boolean handleLabel(DataKind kind, ValuesDelta entry, RawContactDelta state) {

        String accountType = state.getAccountType();

        if (SimAasSneUtils.isSimOrRuim(accountType) && SimAasSneUtils.isPhoneNumType(
                kind.mimeType)) {
            Log.d(TAG, "[handleLabel] hide label for sim card or Ruim");
            return true;
        }

        if (SimAasSneUtils.isUsimOrCsim(accountType) && SimAasSneUtils.isPhoneNumType(kind.mimeType)
                && !SimAasSneUtils.IS_ADDITIONAL_NUMBER.equals(entry.getAsString(
                        Data.IS_ADDITIONAL_NUMBER))) {
            // primary number, hide phone label
            Log.d(TAG, "[handleLabel] hide label for primary number.");
            return true;
        }
        if (SimAasSneUtils.isUsimOrCsim(accountType)
                && Email.CONTENT_ITEM_TYPE.equals(kind.mimeType)) {
            Log.d(TAG, "[handleLabel] hide label for email");
            return true;
        }

        return false;
    }

    public ArrayList<ValuesDelta> rebuildFromState(RawContactDelta state, String mimeType) {

        String accountType = state.getAccountType();
        if (SimAasSneUtils.isPhoneNumType(mimeType)) {
            ArrayList<ValuesDelta> values = state.getMimeEntries(mimeType);
            if (values != null) {
                ArrayList<ValuesDelta> orderedDeltas = new ArrayList<ValuesDelta>();
                for (ValuesDelta entry : values) {
                    if (isAdditionalNumber(entry)) {
                        orderedDeltas.add(entry);
                    } else {
                        if (SimAasSneUtils.isUsimOrCsim(accountType)) {
                            // add primary number to first.
                            orderedDeltas.add(0, entry);
                        } else {
                            orderedDeltas.add(entry);
                        }
                    }
                }
                return orderedDeltas;
            }
        }
        return state.getMimeEntries(mimeType);
    }

    public boolean updateView(RawContactDelta state, View view, ValuesDelta entry, int action) {
        int type = (entry == null) ? 0 : (isAdditionalNumber(entry) ? 1 : 0);
        String accountType = state.getAccountType();
        Log.d(TAG, "[updateView] type=" + type + ",action=" + action + ",accountType="
                + accountType);
        switch (action) {
        case VIEW_UPDATE_HINT:
            if (SimAasSneUtils.isUsimOrCsim(accountType)) {
                if (view instanceof TextView) {
                    if (type == STRING_PRIMART) {
                        ((TextView) view).setHint(R.string.aas_phone_primary);
                    } else if (type == STRING_ADDITINAL) {
                        ((TextView) view).setHint(R.string.aas_phone_additional);
                    }
                } else {
                    Log.e(TAG, "[updateView]  VIEW_UPDATE_HINT but view is not a TextView");
                }
            }
            break;
        case VIEW_UPDATE_VISIBILITY:
            if (SimAasSneUtils.isUsimOrCsim(accountType)) {
                view.setVisibility(View.GONE);
            } else {
                return false;
            }
            break;
        case VIEW_UPDATE_DELETE_EDITOR:
            if (!SimAasSneUtils.isUsimOrCsim(accountType)) {
                return false;
            }
            break;

            default:
                break;
        }
        return true;
    }

    public int getMaxEmptyEditors(RawContactDelta state, String mimeType) {
        String accountType = state.getAccountType();
        Log.d(TAG, "[getMaxEmptyEditors] accountType=" + accountType + ",mimeType=" + mimeType);
        if (SimAasSneUtils.isUsimOrCsim(accountType)
                && SimAasSneUtils.isPhoneNumType(mimeType)) {
            //Host at the entry have already set for subId, we will get subId from simutils
            int subId = SimAasSneUtils.getCurSubId();
            int max = SlotUtils.getUsimAnrCount(subId) + 1;
            Log.d(TAG, "[getMaxEmptyEditors] max=" + max);
            return max;
        }
        Log.d(TAG, "[getMaxEmptyEditors] max= 1");
        return 1;
    }

    public String getCustomTypeLabel(int type, String customColumn) {
        Log.d(TAG, "[getCustomTypeLabel] type=" + type + ",customColumn="
                + customColumn + ",subId=" + SimAasSneUtils.getCurSubId());
        if (SimAasSneUtils.isUsimOrCsim(SimAasSneUtils.getCurAccount())
                && SimAasSneUtils.isAasPhoneType(type)) {
            if (!TextUtils.isEmpty(customColumn)) {
                CharSequence tag = Phone.getTypeLabel(mContext, type, customColumn);
                Log.d(TAG, "[getCustomTypeLabel] index" + customColumn + " tag=" + tag);
                return tag.toString();
            }
        }
        return null;
    }

    public boolean rebuildLabelSelection(RawContactDelta state, Spinner label,
            ArrayAdapter<EditType> adapter, EditType item, DataKind kind) {
        if (item == null || kind == null) {
            label.setSelection(adapter.getPosition(item));
            return false;
        }
        if (SimAasSneUtils.isUsimOrCsim(state.getAccountType())
                && SimAasSneUtils.isPhoneNumType(kind.mimeType)
                && SimAasSneUtils.isAasPhoneType(item.rawValue)) {
            for (int i = 0; i < adapter.getCount(); i++) {
                EditType type = adapter.getItem(i);
                if (type.customColumn != null && type.customColumn.equals(item.customColumn)) {
                    label.setSelection(i);
                    Log.d(TAG, "[rebuildLabelSelection] position=" + i);
                    return true;
                }
            }
        }
        label.setSelection(adapter.getPosition(item));
        return false;
    }

    public boolean onTypeSelectionChange(RawContactDelta rawContact, ValuesDelta entry,
            DataKind kind, ArrayAdapter<EditType> editTypeAdapter,
            EditType select, EditType type) {
        return onTypeSelectionChange(rawContact, entry, kind, editTypeAdapter, select,
                  type, null);
    }

    public boolean onTypeSelectionChange(RawContactDelta rawContact, ValuesDelta entry,
            DataKind kind, ArrayAdapter<EditType> editTypeAdapter,
            EditType select, EditType type, Context context) {
        String accountType = rawContact.getAccountType();
        Log.d(TAG, "[onTypeSelectionChange] Entry: accountType= " + accountType);
        if (SimAasSneUtils.isUsimOrCsim(accountType)
                && SimAasSneUtils.isPhoneNumType(kind.mimeType)) {
            if (type == select) {
                Log.i(TAG, "[onTypeSelectionChange] same select");
                return true;
            }
            if (Phone.TYPE_CUSTOM == select.rawValue) {
                Log.i(TAG, "[onTypeSelectionChange] Custom Selected");
                onTypeSelectionChange(context, select.rawValue);
            } else {
                type = select; // modifying the type of host app so passed in para
                Log.i(TAG, "[onTypeSelectionChange] different Selected");
                entry.put(kind.typeColumn, type.rawValue);
                // insert aas index to entry.
                updatemEntryValue(entry, type);
            }
            return true;
        }
        return false;
    }

    private void onTypeSelectionChange(Context context, int position) {
        Log.d(TAG, "[onTypeSelectionChange] private");
        if (SimAasSneUtils.isUsimOrCsim(SimAasSneUtils.getCurAccount())) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setAction("com.mediatek.contacts.action.EDIT_AAS");
            int subId = SimAasSneUtils.getCurSubId();
            Log.d(TAG, "[onTypeSelectionChange] internal: subId to fill in slot_key= " + subId);
            intent.putExtra(SimAasSneUtils.KEY_SUB_ID, subId);
            Log.d(TAG, "[onTypeSelectionChange] call for startActivity");
            /*
             * [ALPS03564655] When task is started from other App more than Contacts
             * the activity will start a new task or merge to existing contacts task
             * for mContext is not activity. This will make the activity return to
             * contacts unexpectedly while it is started by others.
             */
            if (context != null) {
                context.startActivity(intent);
            } else {
                mContext.startActivity(intent);
            }
        }
    }

    public EditType getCurrentType(ValuesDelta entry, DataKind kind, int rawValue) {
        if (SimAasSneUtils.isAasPhoneType(rawValue)) {
            Log.d(TAG, "[getCurrentType] return getAasEditType");
            return getAasEditType(entry, kind, rawValue);
        }
        Log.d(TAG, "[getCurrentType]calling default");
        return RawContactModifier.getType(kind, rawValue);
    }

    public static boolean updatemEntryValue(ValuesDelta entry, EditType type) {
        if (SimAasSneUtils.isAasPhoneType(type.rawValue)) {
            entry.put(Phone.LABEL, type.customColumn);
            return true;
        }
        return false;
    }

    // -----------for SIMImportProcessor.java start--------------//
    // interface will be called when USIM part are copied from USIM to create
    // master database
    public void updateOperation(String accountType, ContentProviderOperation.Builder builder,
            Cursor cursor, int type) {
        Log.d(TAG, "[updateOperation] Entry type: " + type + ",accountType: " + accountType);
        switch (type) {
        case TYPE_FOR_ADDITIONAL_NUMBER:
            checkAasOperationBuilder(accountType, builder, cursor);
        }
    }

    private boolean checkAasOperationBuilder(String accountType,
            ContentProviderOperation.Builder builder, Cursor cursor) {
        if (SimAasSneUtils.isUsimOrCsim(accountType)) {
            int aasColumn = cursor.getColumnIndex("aas");
            Log.d(TAG, "[checkAasOperationBuilder] aasColumn " + aasColumn);
            if (aasColumn >= 0) {
                String aas = cursor.getString(aasColumn);
                Log.d(TAG, "[checkAasOperationBuilder] aas " + aas);
                builder.withValue(Data.DATA2, Anr.TYPE_AAS);
                builder.withValue(Data.DATA3, aas);
            }
            return true;
        }
        return false;
    }

    // -----------for SIMImportProcessor.java ends--------------//

    // -----------for CopyProcessor.java starts--------//

    private boolean buildAnrOperation(String accountType,
            ArrayList<ContentProviderOperation> operationList,
            ArrayList anrList, int backRef) {
        if (SimAasSneUtils.isUsimOrCsim(accountType)) {
            // build Anr ContentProviderOperation
            for (Object obj : anrList) {
                Anr anr = (Anr) obj;
                if (!TextUtils.isEmpty(anr.mAdditionNumber)) {
                    Log.d(TAG, "[buildAnrOperation] additionalNumber=" + anr.mAdditionNumber +
                            " aas=" + anr.mAasIndex);

                    ContentProviderOperation.Builder builder = ContentProviderOperation
                            .newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, backRef);
                    builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    builder.withValue(Data.DATA2, Anr.TYPE_AAS);
                    int aasIndex = SimAasSneUtils.getAasIndexByName(anr.mAasIndex,
                            SimAasSneUtils.getCurSubId());

                    builder.withValue(Phone.NUMBER, anr.mAdditionNumber);
                    // builder.withValue(Data.DATA3, anr.mAasIndex);
                    builder.withValue(Data.DATA3, String.valueOf(aasIndex));

                    builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
                    operationList.add(builder.build());
                }
            }
            return true;
        }
        return false;
    }

    private int mCopyCount = 0;
    private ArrayList<Anr> additionalArray = new ArrayList<Anr>();

    public void updateValuesforCopy(Uri sourceUri, int subId, String accountType,
            ContentValues simContentValues) {

        Log.d(TAG, "[updateValuesforCopy] Entry");
        SimAasSneUtils.setCurrentSubId(subId);

        if (!SimAasSneUtils.isUsimOrCsim(accountType)) {
            Log.d(TAG, "[updateValuesforCopy] return account is not USIM");
            return;
        }

        mInsertFlag = OPERATION_CONTACT_COPY;
        // if (mCopyUri != sourceUri)
        if (mCopyCount == 0) {
            ArrayList<Anr> phoneArray = new ArrayList<Anr>();

            ContentResolver resolver = mContext.getContentResolver();
            final String[] newProjection = new String[] { Contacts._ID, Contacts.Data.MIMETYPE,
                    Contacts.Data.DATA1, Contacts.Data.IS_ADDITIONAL_NUMBER, Contacts.Data.DATA2,
                    Contacts.Data.DATA3 };

            Cursor c = resolver.query(sourceUri, newProjection, null, null, null);
            if (c != null && c.moveToFirst()) {
                do {
                    String mimeType = c.getString(1);
                    if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                        String number = c.getString(2);
                        Anr entry = new Anr();

                        entry.mAdditionNumber = c.getString(2);
                        Log.d(TAG, "[updateValuesforCopy] simAnrNum:" + entry.mAdditionNumber);

                        entry.mAasIndex = mContext.getString(Phone
                                .getTypeLabelResource(c.getInt(4)));
                        Log.d(TAG, "[updateValuesforCopy] aasIndex:" + entry.mAasIndex);

                        if (c.getInt(3) == 1) {
                            additionalArray.add(entry);
                        } else {
                            phoneArray.add(entry);
                        }

                    }
                } while (c.moveToNext());
            }
            if (c != null) {
                c.close();
            }

            if (phoneArray.size() > 0) {
                phoneArray.remove(0); // This entry is handled by host app for
                // primary
                // number
            }
            additionalArray.addAll(phoneArray);
            mCopyCount = additionalArray.size();

        } else {
            additionalArray.remove(0);
            mCopyCount--;
        }

        int uSimMaxAnrCount = SlotUtils.getUsimAnrCount(subId);
        // use this count in case of multiple anr
        int count = additionalArray.size() > uSimMaxAnrCount ? uSimMaxAnrCount : additionalArray
                .size();

        mCopyAnrList = new ArrayList<Anr>();

        for (int i = 0; i < count; i++) {

            Anr entry = additionalArray.remove(0);
            int aasIndex = SimAasSneUtils.getAasIndexByName(entry.mAasIndex, subId);
            Log.d(TAG, "[updateValuesforCopy] additionalNumber:" + entry.mAdditionNumber);
            entry.mAdditionNumber = TextUtils.isEmpty(entry.mAdditionNumber) ? ""
                    : entry.mAdditionNumber.replace("-", "");
            Log.d(TAG, "[updateValuesforCopy] aasIndex:" + aasIndex);
            simContentValues.put("anr" + SimAasSneUtils.getSuffix(i),
                    PhoneNumberUtils.stripSeparators(entry.mAdditionNumber));
            simContentValues.put("aas" + SimAasSneUtils.getSuffix(i), aasIndex);
            mCopyAnrList.add(entry);
            mCopyCount--;
        }

    }

    public boolean cursorColumnToBuilder(Cursor srcCursor, Builder destBuilder,
            String srcAccountType, String srcMimeType, int destSubId, int indexOfColumn) {
        String[] columnNames = srcCursor.getColumnNames();
        return generateDataBuilder(null, srcCursor, destBuilder, columnNames, srcAccountType,
                srcMimeType, destSubId, indexOfColumn);
    }

    public boolean generateDataBuilder(Context context, Cursor dataCursor,
            Builder builder, String[] columnNames, String accountType,
            String mimeType, int destSubId, int index) {
        if (SimAasSneUtils.isUsimOrCsim(accountType) && SimAasSneUtils.isPhoneNumType(mimeType)) {
            String isAnr = dataCursor.getString(dataCursor
                    .getColumnIndex(Data.IS_ADDITIONAL_NUMBER));

            if (Data.DATA2.equals(columnNames[index])) {
                Log.d(TAG, "[generateDataBuilder] isAnr:" + isAnr);
                if ("1".equals(isAnr)) {
                    builder.withValue(Data.DATA2, Phone.TYPE_OTHER);
                    Log.d(TAG, "[generateDataBuilder] DATA2 to be TYPE_OTHER ");
                } else {
                    builder.withValue(Data.DATA2, Phone.TYPE_MOBILE);
                    Log.d(TAG, "[generateDataBuilder] DATA2 to be TYPE_MOBILE ");
                }
                return true;
            }
            if (Data.DATA3.equals(columnNames[index])) {
                Log.d(TAG, "[generateDataBuilder] DATA3 to be null");
                builder.withValue(Data.DATA3, null);
                return true;
            }
        }
        return false;
    }

    // -----------for CopyProcessor.java ends--------//
    // -------------------for SIMEditProcessor.java starts-------------//
    // this interface to check whether a entry is of additional number or not
    // for SIM & USIM
    public boolean checkAasEntry(ContentValues cv) {
        Log.d(TAG, "[checkAasEntry] para = " + cv);
        if (isAdditionalNumber(cv)) {
            return true;
        }
        return false;
    }

    public String getSubheaderString(int subId, int type) {
        Log.d(TAG, "[getSubheaderString] subId = " + subId);
        if (subId == -1) {
            Log.d(TAG, "[getSubheaderString] Phone contact");
            return null;
        }
        String accountType = SimAasSneUtils.getAccountTypeBySub(subId);
        if (SimAasSneUtils.isUsimOrCsim(accountType)) {
            if (SimAasSneUtils.isAasPhoneType(type)) {
                Log.d(TAG, "[getSubheaderString] USIM additional number");
                return mContext.getResources().getString(R.string.aas_phone_additional);

            } else {
                Log.d(TAG, "[getSubheaderString] USIM primary number ");
                return mContext.getResources().getString(R.string.aas_phone_primary);
            }
        }
        Log.d(TAG, "[getSubheaderString] Account is SIM ");
        return null;

    }

    // this interface to update additional number & aasindex while writing or
    // updating the USIMcard contact
    public boolean updateValues(Intent intent, int subId, ContentValues contentValues) {
        Log.d(TAG, "[updateValues] Entry.");
        ArrayList<RawContactDelta> newSimData = new ArrayList<RawContactDelta>();
        newSimData = intent.getParcelableArrayListExtra(SimServiceUtils.KEY_SIM_DATA);
        ArrayList<RawContactDelta> oldSimData = new ArrayList<RawContactDelta>();
        oldSimData = intent.getParcelableArrayListExtra(SimServiceUtils.KEY_OLD_SIM_DATA);
        String accountType = newSimData.get(0).getValues().getAsString(RawContacts.ACCOUNT_TYPE);
        SimAasSneUtils.setCurrentSubId(subId);
        if (!SimAasSneUtils.isUsimOrCsim(accountType)) {
            Log.d(TAG, "[updateValues] Account type is not USIM.");
            return false;
        }

        // case of new contact
        if (oldSimData == null) {
            // put values for anr
            // aas as anr.mAasIndex
            // set the mInsertFlag to insert, will be used later in
            // updateoperationList, prepare newanrlist
            Log.d(TAG, "[updateValues] for new contact.");
            mInsertFlag = OPERATION_CONTACT_INSERT;
            prepareNewAnrList(intent);
            Log.d(TAG, "[updateValues] for new contact Newanrlist filled");
            return buildAnrInsertValues(accountType, contentValues, mAnrsList);
        }
        // case of edit contact
        else {

            // put values for newAnr
            // aas as anr.mAasIndex
            // set the mInsertFlag to edit, prepare old & new both anr list
            Log.d(TAG, "[updateValues] for Edit contact.");
            mInsertFlag = OPERATION_CONTACT_EDIT;
            prepareNewAnrList(intent);
            Log.d(TAG, "[updateValues] for New anrlist filled");
            prepareOldAnrList(intent);
            Log.d(TAG, "[updateValues] for Old anrlist filled");
            return buildAnrUpdateValues(accountType, contentValues, mAnrsList);
        }

    }

    private void prepareNewAnrList(Intent intent) {
        ArrayList<RawContactDelta> newSimData = new ArrayList<RawContactDelta>();
        newSimData = intent.getParcelableArrayListExtra(SimServiceUtils.KEY_SIM_DATA);
        mAnrsList.clear();
        if (newSimData == null) {
            return;
        }
        // now fill tha data for newanrlist
        int kindCount = newSimData.get(0).getContentValues().size();
        String mimeType = null;
        String data = null;
        for (int countIndex = 0; countIndex < kindCount; countIndex++) {
            mimeType = newSimData.get(0).getContentValues().get(countIndex)
                    .getAsString(Data.MIMETYPE);
            data = newSimData.get(0).getContentValues().get(countIndex).getAsString(Data.DATA1);
            Log.d(TAG, "[prepareNewAnrList]countIndex:" + countIndex + ",mimeType:" + mimeType
                    + "data:" + data);
            if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final ContentValues cv = newSimData.get(0).getContentValues().get(countIndex);
                if (isAdditionalNumber(cv)) {
                    Anr addPhone = new Anr();
                    addPhone.mAdditionNumber = replaceCharOnNumber(cv.getAsString(Data.DATA1));
                    addPhone.mAasIndex = cv.getAsString(Data.DATA3);
                    Log.d(TAG, "[prepareNewAnrList] mAdditionNumber:" + addPhone.mAdditionNumber
                            + ",mAasIndex:" + addPhone.mAasIndex);
                    mAnrsList.add(addPhone);
                }

            }

        }

    }

    private void prepareOldAnrList(Intent intent) {
        ArrayList<RawContactDelta> oldSimData = new ArrayList<RawContactDelta>();
        oldSimData = intent.getParcelableArrayListExtra(SimServiceUtils.KEY_OLD_SIM_DATA);
        mOldAnrsList.clear();
        if (oldSimData == null) {
            return;
        }
        // now fill the data for oldAnrlist
        int oldCount = oldSimData.get(0).getContentValues().size();
        String mimeType = null;
        String data = null;
        for (int oldIndex = 0; oldIndex < oldCount; oldIndex++) {
            mimeType = oldSimData.get(0).getContentValues().get(oldIndex)
                    .getAsString(Data.MIMETYPE);
            data = oldSimData.get(0).getContentValues().get(oldIndex).getAsString(Data.DATA1);
            Log.d(TAG, "[prepareOldAnrList]Data.MIMETYPE: " + mimeType + ",data:" + data);
            if (Phone.CONTENT_ITEM_TYPE.equals(mimeType)) {
                ContentValues cv = oldSimData.get(0).getContentValues().get(oldIndex);
                if (isAdditionalNumber(cv)) {
                    Anr addPhone = new Anr();
                    addPhone.mAdditionNumber = replaceCharOnNumber(cv.getAsString(Data.DATA1));
                    addPhone.mAasIndex = cv.getAsString(Phone.DATA3);
                    Log.d(TAG, "[prepareOldAnrList] mAdditionNumber:" + addPhone.mAdditionNumber
                            + ",mAasIndex: " + addPhone.mAasIndex);
                    addPhone.mId = cv.getAsInteger(Data._ID);
                    mOldAnrsList.add(addPhone);
                }
            }

        }

    }

    private boolean buildAnrInsertValues(String accountType,
            ContentValues values, ArrayList anrsList) {
        if (SimAasSneUtils.isUsimOrCsim(accountType)) {
            int count = 0;
            for (Object obj : anrsList) {
                Anr anr = (Anr) obj;
                String additionalNumber = TextUtils.isEmpty(anr.mAdditionNumber) ? ""
                        : anr.mAdditionNumber;
                String additionalNumberToInsert = additionalNumber;
                if (!TextUtils.isEmpty(additionalNumber)) {
                    additionalNumberToInsert = PhoneNumberUtils.stripSeparators(additionalNumber);
                    if (!Pattern.matches(SIM_NUM_PATTERN,
                            PhoneNumberUtils.extractCLIRPortion(additionalNumberToInsert))) {
                        boolean resultInvalidNumber = true;
                        Log.d(TAG, "[buildAnrInsertValues] additionalNumber Invalid ");
                    }
                    Log.d(TAG, "[buildAnrInsertValues] additionalNumber updated : "
                            + additionalNumberToInsert);
                }
                values.put("anr" + SimAasSneUtils.getSuffix(count), additionalNumberToInsert);
                values.put("aas" + SimAasSneUtils.getSuffix(count),
                        getAasIndexFromIndicator(anr.mAasIndex));
                count++;
                Log.d(TAG, "[buildAnrInsertValues] aasIndex=" + anr.mAasIndex
                        + ", additionalNumber=" + additionalNumber);
            }
            return true;
        }
        return false;
    }

    private boolean buildAnrUpdateValues(String accountType,
            ContentValues updatevalues, ArrayList<Anr> anrsList) {
        if (SimAasSneUtils.isUsimOrCsim(accountType)) {
            int count = 0;
            for (Anr anr : anrsList) {
                Log.d(TAG, "[buildAnrUpdateValues] additionalNumber : "
                        + anr.mAdditionNumber);
                if (!TextUtils.isEmpty(anr.mAdditionNumber)) {
                    String additionalNumber = anr.mAdditionNumber;
                    String additionalNumberToInsert = additionalNumber;
                    additionalNumberToInsert = PhoneNumberUtils
                            .stripSeparators(additionalNumber);
                    if (!Pattern.matches(SIM_NUM_PATTERN, PhoneNumberUtils
                            .extractCLIRPortion(additionalNumberToInsert))) {
                        boolean resultInvalidNumber = true;
                        Log.d(TAG,
                                "[buildAnrUpdateValues] additionalNumber Invalid");
                    }
                    Log.d(TAG,
                            "[buildAnrUpdateValues] additionalNumber updated: "
                                    + additionalNumberToInsert);
                    updatevalues.put("newAnr" + SimAasSneUtils.getSuffix(count),
                            additionalNumberToInsert);
                    updatevalues.put("aas" + SimAasSneUtils.getSuffix(count),
                            getAasIndexFromIndicator(anr.mAasIndex));
                }
                count++;
            }
            return true;
        }
        return false;
    }

    public boolean updateAdditionalNumberToDB(Intent intent, long rawContactId) {
        ArrayList<RawContactDelta> newSimData = new ArrayList<RawContactDelta>();
        ArrayList<RawContactDelta> oldSimData = new ArrayList<RawContactDelta>();
        Log.d(TAG, "[updateAdditionalNumberToDB] Entry");

        newSimData = intent.getParcelableArrayListExtra(SimServiceUtils.KEY_SIM_DATA);
        oldSimData = intent.getParcelableArrayListExtra(SimServiceUtils.KEY_OLD_SIM_DATA);
        String accountType = newSimData.get(0).getValues()
                .getAsString(RawContacts.ACCOUNT_TYPE);

        if (!SimAasSneUtils.isUsimOrCsim(accountType)) {
            Log.d(TAG,
                    "[updateAdditionalNumberToDB] return false, account is not USIM");
            return false;
        }
        ContentResolver resolver = mContext.getContentResolver();
        // amit todo check whether passed anrlist & oldanr list are already
        // filled correctly by prevuious interfaces or
        // do we need to take it from intent
        Log.d(TAG, "[updateAdditionalNumberToDB] mAnrlist:" + mAnrsList + ",mOldAnrsList: "
                + mOldAnrsList);
        return updateAnrToDb(accountType, resolver, mAnrsList, mOldAnrsList,
                rawContactId);

    }

    private boolean updateAnrToDb(String accountType, ContentResolver resolver,
            ArrayList anrsList, ArrayList oldAnrsList, long rawId) {
        if (SimAasSneUtils.isUsimOrCsim(accountType)) {
            String whereadditional = Data.RAW_CONTACT_ID + " = \'" + rawId
                    + "\'" + " AND " + Data.MIMETYPE + "='"
                    + Phone.CONTENT_ITEM_TYPE + "'" + " AND "
                    + Data.IS_ADDITIONAL_NUMBER + " =1" + " AND " + Data._ID
                    + " =";
            Log.d(TAG, "[updateAnrInfoToDb] whereadditional:"
                    + whereadditional);

            // Here, mAnrsList.size() should be the same as mOldAnrsList.size()
            int newSize = anrsList.size();
            int oldSize = oldAnrsList.size();
            int count = Math.min(newSize, oldSize);
            String additionNumber;
            String aas;
            String oldAdditionNumber;
            String oldAas;
            long dataId;
            String where;
            ContentValues additionalvalues = new ContentValues();

            int i = 0;
            for (; i < count; i++) {
                Anr newAnr = (Anr) anrsList.get(i);
                Anr oldAnr = (Anr) oldAnrsList.get(i);
                where = whereadditional + oldAnr.mId;

                additionalvalues.clear();
                if (!TextUtils.isEmpty(newAnr.mAdditionNumber)
                        && !TextUtils.isEmpty(oldAnr.mAdditionNumber)) { // update
                    additionalvalues.put(Phone.NUMBER, newAnr.mAdditionNumber);
                    additionalvalues.put(Data.DATA2, Anr.TYPE_AAS);
                    additionalvalues.put(Data.DATA3, newAnr.mAasIndex);

                    int upadditional = resolver.update(Data.CONTENT_URI,
                            additionalvalues, where, null);
                    Log.d(TAG, "[updateAnrInfoToDb] upadditional: " + upadditional);
                } else if (!TextUtils.isEmpty(newAnr.mAdditionNumber)
                        && TextUtils.isEmpty(oldAnr.mAdditionNumber)) { // insert
                    additionalvalues.put(Phone.RAW_CONTACT_ID, rawId);
                    additionalvalues
                            .put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    additionalvalues.put(Phone.NUMBER, newAnr.mAdditionNumber);
                    additionalvalues.put(Data.DATA2, Anr.TYPE_AAS);
                    additionalvalues.put(Data.DATA3, newAnr.mAasIndex);
                    additionalvalues.put(Data.IS_ADDITIONAL_NUMBER, 1);

                    Uri upAdditionalUri = resolver.insert(Data.CONTENT_URI,
                            additionalvalues);
                    Log.d(TAG, "[updateAnrInfoToDb] upAdditionalUri: " + upAdditionalUri);
                } else if (TextUtils.isEmpty(newAnr.mAdditionNumber)) { // delete
                    int deleteAdditional = resolver.delete(Data.CONTENT_URI,
                            where, null);
                    Log.d(TAG, "[updateAnrInfoToDb] deleteAdditional:" + deleteAdditional);
                }
            }

            // in order to avoid error, do the following operations.
            while (i < oldSize) { // delete one
                Anr oldAnr = (Anr) oldAnrsList.get(i);
                dataId = oldAnr.mId;
                where = whereadditional + dataId;
                int deleteAdditional = resolver.delete(Data.CONTENT_URI, where,
                        null);
                Log.d(TAG, "[updateAnrInfoToDb] deleteAdditional:" + deleteAdditional);
                i++;
            }

            while (i < newSize) { // insert one
                Anr newAnr = (Anr) anrsList.get(i);
                additionalvalues.clear();
                if (!TextUtils.isEmpty(newAnr.mAdditionNumber)) {
                    additionalvalues.put(Phone.RAW_CONTACT_ID, rawId);
                    additionalvalues
                            .put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    additionalvalues.put(Phone.NUMBER, newAnr.mAdditionNumber);
                    additionalvalues.put(Data.DATA2, Anr.TYPE_AAS);
                    additionalvalues.put(Data.DATA3, newAnr.mAasIndex);
                    additionalvalues.put(Data.IS_ADDITIONAL_NUMBER, 1);

                    Uri upAdditionalUri = resolver.insert(Data.CONTENT_URI,
                            additionalvalues);
                    Log.d(TAG, "[updateAnrInfoToDb]upAdditionalUri: " + upAdditionalUri);
                }
                i++;
            }
            return true;
        }
        return false;
    }

    // writing a common api interface for copy & insert as we have to update the
    // operation list, based on mInsertFlag will decide which list have to
    // process
    public boolean updateOperationList(Account accounType,
            ArrayList<ContentProviderOperation> operationList, int backRef) {
        // bassed on op we have to decide which list we have to parse
        // for copy
        if (!SimAasSneUtils.isUsimOrCsim(accounType.type)) {
            Log.d(TAG,
                    "[updateOperationList] Account is not USIM so return false");
            return false;
        }

        if (mInsertFlag == OPERATION_CONTACT_COPY) {
            if (mCopyAnrList != null && mCopyAnrList.size() > 0) {
                Log.d(TAG, "[updateOperationList] for copy ");
                boolean result = buildAnrOperation(accounType.type,
                        operationList, mCopyAnrList, backRef);
                Log.d(TAG, "[updateOperationList] result : " + result);
                mCopyAnrList.clear();
                mCopyAnrList = null;
                return result;
            }
            Log.d(TAG, "[updateOperationList] result false");
            return false;

        }

        // for insert
        else {
            if (SimAasSneUtils.isUsimOrCsim(accounType.type)) {
                Log.d(TAG, "[updateOperationList] for Insert contact ");
                // build Anr ContentProviderOperation
                for (Object obj : mAnrsList) {
                    Anr anr = (Anr) obj;
                    if (!TextUtils.isEmpty(anr.mAdditionNumber)) {
                        Log.d(TAG, "[updateOperationList] additionalNumber="
                                + anr.mAdditionNumber + ",aas=" + anr.mAasIndex);

                        ContentProviderOperation.Builder builder = ContentProviderOperation
                                .newInsert(Data.CONTENT_URI);
                        builder.withValueBackReference(Phone.RAW_CONTACT_ID,
                                backRef);
                        builder.withValue(Data.MIMETYPE,
                                Phone.CONTENT_ITEM_TYPE);
                        builder.withValue(Data.DATA2, Anr.TYPE_AAS);
                        builder.withValue(Phone.NUMBER, anr.mAdditionNumber);
                        builder.withValue(Data.DATA3, anr.mAasIndex);

                        builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
                        operationList.add(builder.build());
                    }
                }
                Log.d(TAG, "[updateOperationList] result true");
                return true;
            }

        }
        Log.d(TAG, "[updateOperationList] result false");
        return false;

    }

    public CharSequence getLabelForBindData(Resources res, int type,
            String customLabel, String mimeType, Cursor cursor,
            CharSequence defaultValue) {

        Log.d(TAG, "[getLabelForBindData] Entry mimetype:" + mimeType);
        CharSequence label = defaultValue;
        final int indicate = cursor.getColumnIndex(Contacts.INDICATE_PHONE_SIM);
        int subId = -1;
        if (indicate != -1) {
            subId = cursor.getInt(indicate);
        }
        String accountType = SimAasSneUtils.getAccountTypeBySub(subId);
        if (SimAasSneUtils.isUsimOrCsim(accountType)
                && mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
            label = "";
        } else {

            label = getTypeLabel(type, (CharSequence) customLabel,
                    (String) defaultValue, subId);

        }
        return label;

    }

    private String replaceCharOnNumber(String number) {
        String trimNumber = number;
        if (!TextUtils.isEmpty(trimNumber)) {
            Log.d(TAG, "[replaceCharOnNumber]befor replaceall number : "
                    + trimNumber);
            trimNumber = trimNumber.replaceAll("-", "");
            trimNumber = trimNumber.replaceAll(" ", "");
            Log.d(TAG, "[replaceCharOnNumber]after replaceall number : " + trimNumber);
        }
        return trimNumber;
    }

    public CharSequence getTypeLabel(int type, CharSequence label,
            String defvalue, int subId) {
        String accountType = SimAasSneUtils.getAccountTypeBySub(subId);
        Log.d(TAG, "[getTypeLabel] subId=" + subId + " accountType="
                + accountType);
        if (SimAasSneUtils.isSim(accountType) || SimAasSneUtils.isRuim(accountType)) {
            Log.d(TAG, "[getTypeLabel] SIM Account no Label.");
            return "";
        }
        if (SimAasSneUtils.isUsimOrCsim(accountType) && SimAasSneUtils.isAasPhoneType(type)) {
            Log.d(TAG, "[getTypeLabel] USIM Account label=" + label);
            if (TextUtils.isEmpty(label)) {
                Log.d(TAG, "[getTypeLabel] Empty");
                return "";
            }
            try {
                CharSequence tag = Phone.getTypeLabel(mContext, type, label);
                Log.d(TAG, "[getTypeLabel] label" + label + " tag=" + tag);
                return tag;
            } catch (NumberFormatException e) {
                Log.e(TAG, "[getTypeLabel] return label=" + label);
            }
        }
        if (SimAasSneUtils.isUsimOrCsim(accountType) && !SimAasSneUtils.isAasPhoneType(type)) {
            Log.d(TAG, "[getTypeLabel] account is USIM but type is not additional");
            return "";
        }
        return defvalue;
    }

    // Amit ends

    private boolean isAdditionalNumber(ValuesDelta entry) {
        final String key = Data.IS_ADDITIONAL_NUMBER;
        Integer isAnr = entry.getAsInteger(key);
        return isAnr != null && 1 == isAnr.intValue();
    }

    private boolean isAdditionalNumber(final ContentValues cv) {
        final String key = Data.IS_ADDITIONAL_NUMBER;
        Integer isAnr = null;
        if (cv != null && cv.containsKey(key)) {
            isAnr = cv.getAsInteger(key);
        }
        return isAnr != null && 1 == isAnr.intValue();
    }

    private EditType getAasEditType(ValuesDelta entry, DataKind kind,
            int phoneType) {
        if (phoneType == Anr.TYPE_AAS) {
            String customColumn = entry.getAsString(Data.DATA3);
            Log.d(TAG, "[getAasEditType] customColumn=" + customColumn);
            if (customColumn != null) {
                for (EditType type : kind.typeList) {
                    if (type.rawValue == Anr.TYPE_AAS
                            && customColumn.equals(type.customColumn)) {
                        Log.d(TAG, "[getAasEditType] type = " + type.toString());
                        return type;
                    }
                }
            }
            return null;
        }
        Log.e(TAG, "[getAasEditType] error Not Anr.TYPE_AAS, type=" + phoneType);
        return null;
    }

    public void ensurePhoneKindForCompactEditor(RawContactDeltaList state,
            int subId, Context context) {

        int numRawContacts = state.size();
        Log.d(TAG, "[ensurePhoneKindForCompactEditor] Entry numRawContacts= " + numRawContacts);
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        for (int i = 0; i < numRawContacts; i++) {
            final RawContactDelta rawContactDelta = state.get(i);
            final AccountType type = rawContactDelta.getAccountType(accountTypes);
            Log.d(TAG, "[ensurePhoneKindForCompactEditor] loop subid=" + subId + ",type: " + type);
            ensurePhoneKindForEditor(type, subId, rawContactDelta);
        }
    }

    public static int getAasIndexFromIndicator(String indicator) {
        if(!TextUtils.isEmpty(indicator) && indicator.contains(Aas.ENCODE_SYMBOL)) {
            String[] keys = indicator.split(Aas.ENCODE_SYMBOL);
            return Integer.valueOf(keys[1]);
        }
        return 0;
    }

    /*
     * fix ALPS02795593.some usim cannot support aas and anr.when change account using
     * oldstate which have addition number column.so should remove it by get anr count.@{
     */
    private void removeAnrFieldIfNotSupportAnr(RawContactDelta state, ArrayList<ValuesDelta> values,
            int slotAnrSize) {
        Log.d(TAG, "[removeAnrFieldIfNotSupportAnr] state:" + state + ",values:" + values +
                ",slotAnrSize:" + slotAnrSize);
        if ((state == null) || (values == null)) {//should judge vaules is or null
            Log.w(TAG, "[removeAnrFieldIfNotSupportAnr] state or value is null,return");
            return;
        }
        final boolean isSupportAnr = slotAnrSize > 0 ? true : false;
        if (!isSupportAnr) {//not support addition number,so remove related Entry if exist
            Iterator<ValuesDelta> iterator = values.iterator();
            while (iterator.hasNext()) {
                ValuesDelta value = iterator.next();
                Integer isAnr = value.getAsInteger(Data.IS_ADDITIONAL_NUMBER);
                if (isAnr != null && (isAnr.intValue() == 1)) {
                    Log.d(TAG, "[removeAnrFieldIfNotSupportAnr] remove vaule: " + value);
                    iterator.remove();
                }
            }
            Log.d(TAG, "[removeAnrFieldIfNotSupportAnr] after state:" + state);
        }
    }
    /* @} */

    /*
     * ALPS03057922. we should remove the vaule including addition_number cloumn for NonSimType
     * ex: local phone & exchange account.@{
     */
    public static void removeRedundantAnrFieldForNonSimAccount(AccountType oldAccountType,
        AccountType newAccountType, RawContactDelta newState, String mimeType) {
        Log.d(TAG, "[removeRedundantAnrFieldForNonSimAccount] oldAccountType:" + oldAccountType +
                ",newAccountType: " + newAccountType + ",newState: " + newState + ",mimeType:"
                + mimeType);
        if ((newAccountType != null) && !AccountTypeUtils.isAccountTypeIccCard(
                newAccountType.accountType)) {
            ArrayList<ValuesDelta> values = newState.getMimeEntries(mimeType);
            removeAnrValueDirectly(values);
        }
        Log.d(TAG, "[removeRedundantAnrFieldForNonSimAccount] result newState:" + newState);
    }

    private static void removeAnrValueDirectly(ArrayList<ValuesDelta> values) {
        Log.d(TAG, "[removeAnrValueDirectly]");
        if (values == null) {
            Log.w(TAG, "[removeAnrValueDirectly] null values,return!");
            return;
        }
        for (ValuesDelta value : values) {
            String data1 = value.getAsString(ContactsContract.Data.DATA1);//get data1 value
            boolean isDataInvalid = TextUtils.isEmpty(data1);
            Integer isAnr = value.getAsInteger(ContactsContract.Data.IS_ADDITIONAL_NUMBER);
            boolean canBeRemove = (isAnr != null) && (isAnr.intValue() == 1);
            if (isDataInvalid && canBeRemove) {
                Log.d(TAG, "[removeAnrFieldDirectly] remove Anr value:" + value);
                values.remove(value);
            }
        }
    }
    /// @}
}
