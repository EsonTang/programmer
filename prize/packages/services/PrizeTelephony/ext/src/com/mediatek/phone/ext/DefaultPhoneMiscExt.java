package com.mediatek.phone.ext;


public class DefaultPhoneMiscExt implements IPhoneMiscExt {

    @Override
    public boolean publishBinderDirectly() {
        return false;

    }

    /**
     * remove "Ask First" item index from call with selection list.
     *
     * @param String[] entryValues
     * @return entryValues after update object
     */
    @Override
    public String[] removeAskFirstFromSelectionListIndex(String[] entryValues) {
        return entryValues;
    }

    /**
     * remove "Ask First" item value from call with selection list.
     *
     * @param String[] entries
     * @return entries after remove object.
     */
    @Override
    public CharSequence[] removeAskFirstFromSelectionListValue(CharSequence[] entries) {
        return entries;
    }


    /**
     * For OP09 Set the selectedIndex to the first one When remove "Ask First".
     *
     * @param selectedIndex the default index
     * @return return the first index of phone account.
     */
    @Override
    public int getSelectedIndex(int selectedIndex) {
        return selectedIndex;
    }

    /**
     * add "Current Network" item index from call with selection list.
     *
     * @param String[] entryValues
     * @return entryValues after update object
     */
    @Override
    public String[] addCurrentNetworkToSelectionListIndex(String[] entryValues) {
        return entryValues;
    }

    /**
     * add "Current Network" item value from call with selection list.
     *
     * @param String[] entries
     * @return entries after add object.
     */
    @Override
    public CharSequence[] addCurrentNetworkToSelectionListValue(CharSequence[] entries) {
        return entries;
    }

    /**
     * For OP18 set the current network setting if current network is selected.
     *
     * @param index the index selected
     * @return return if the value had been handled by the plugin or not.
     */
    @Override
    public boolean onPreferenceChange(int index) {
        return false;
    }

    /**
     * For OP18 get the current highlighted index if current network is selected.
     *
     * @param selectedIndex the default index
     * @return return the first index of phone account.
     */

    @Override
    public int getCurrentNetworkIndex(int index) {
        return index;
    }
}
