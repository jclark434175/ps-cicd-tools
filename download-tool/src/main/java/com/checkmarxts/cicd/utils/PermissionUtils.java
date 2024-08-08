package com.checkmarxts.cicd.utils;

import java.util.HashSet;
import java.util.Set;
import java.nio.file.attribute.PosixFilePermission;


public class PermissionUtils {

    private static HashSet<PosixFilePermission> getSet(int mode, PosixFilePermission read, PosixFilePermission write, PosixFilePermission execute)
    {
        var retval = new HashSet<PosixFilePermission>();

        if ((mode & 0x4) != 0)
            retval.add(read);
        if ((mode & 0x2) != 0)
            retval.add(write);
        if ((mode & 0x1) != 0)
            retval.add(execute);

        return retval;
    }

    private static Set<PosixFilePermission> getUserSet(int user_mode)
    {
        return getSet(user_mode, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);
    }

    private static Set<PosixFilePermission> getGroupSet(int group_mode)
    {
        return getSet(group_mode, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_WRITE, PosixFilePermission.GROUP_EXECUTE);
    }

    private static Set<PosixFilePermission> getOtherSet(int other_mode)
    {
        return getSet(other_mode, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_WRITE, PosixFilePermission.OTHERS_EXECUTE);
    }

    public static Set<PosixFilePermission> translateUnixMode(int all_mode)
    {
        HashSet<PosixFilePermission> retval = new HashSet<PosixFilePermission>();
        retval.addAll(getOtherSet(all_mode & 0x7));
        retval.addAll(getGroupSet(all_mode >> 3));
        retval.addAll(getUserSet(all_mode >> 6));

        return retval;
    }
    
}
