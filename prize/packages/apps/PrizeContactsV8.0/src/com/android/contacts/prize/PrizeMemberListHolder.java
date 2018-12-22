package com.android.contacts.prize;

import com.android.contacts.group.GroupEditorFragment;

import java.util.ArrayList;

/**
 * prize add for bug: crash when add large contacts in GroupEditorActivity by zhaojian 20180419
 */

public class PrizeMemberListHolder {
    public static ArrayList<GroupEditorFragment.Member> listMembersToAdd;
    public static ArrayList<GroupEditorFragment.Member> listMembersToRemove;
    public static ArrayList<GroupEditorFragment.Member> listToDisplay;
}
