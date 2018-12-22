package com.mediatek.cta;

import android.content.pm.PackageParser;

/**
 * CTA package manager local system service interface.
 *
 * @hide Only for use within the system server.
 */
public abstract class CtaPackageManagerInternal {
    public abstract void linkCtaPermissions(PackageParser.Package pkg);
}
