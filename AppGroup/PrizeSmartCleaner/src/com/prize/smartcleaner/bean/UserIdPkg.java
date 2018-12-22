package com.prize.smartcleaner.bean;

public class UserIdPkg {
    public int appIndex = 0;///recentTaskInfo.appInstanceIndex;
    public int uid;
    public int userId;
    public String pkgName;

    public UserIdPkg(int uid, int userId, String pkgName) {
        this.uid = uid;
        this.userId = userId;
        this.pkgName = pkgName;
        this.appIndex = 0;
    }

    public UserIdPkg(int uid, int userId, String pkgName, int appIndex) {
        this.uid = uid;
        this.userId = userId;
        this.pkgName = pkgName;
        this.appIndex = appIndex;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return (obj instanceof UserIdPkg)
                && this.uid == ((UserIdPkg) obj).uid
                && this.userId == ((UserIdPkg) obj).userId
                && this.appIndex == ((UserIdPkg) obj).appIndex
                && this.pkgName.equals(((UserIdPkg) obj).pkgName);
    }
}
