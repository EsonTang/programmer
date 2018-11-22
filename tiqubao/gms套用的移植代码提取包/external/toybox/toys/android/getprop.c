/* getprop.c - Get an Android system property
 *
 * Copyright 2015 The Android Open Source Project

USE_GETPROP(NEWTOY(getprop, ">2Z", TOYFLAG_USR|TOYFLAG_SBIN))

config GETPROP
  bool "getprop"
  default y
  depends on TOYBOX_ON_ANDROID && TOYBOX_SELINUX
  help
    usage: getprop [NAME [DEFAULT]]

    Gets an Android system property, or lists them all.
*/

#define FOR_getprop
#include "toys.h"

#include <sys/system_properties.h>

#include <selinux/android.h>
#include <selinux/label.h>
#include <selinux/selinux.h>

GLOBALS(
  size_t size;
  char **nv; // name/value pairs: even=name, odd=value
  struct selabel_handle *handle;
)

static char *get_property_context(const char *property)
{
  char *context = NULL;

  if (selabel_lookup(TT.handle, &context, property, 1)) {
    perror_exit("unable to lookup label for \"%s\"", property);
  }
  return context;
}

static void read_callback(void *unused, const char *name, const char *value)
{
  if (!(TT.size&31)) TT.nv = xrealloc(TT.nv, (TT.size+32)*2*sizeof(char *));

  TT.nv[2*TT.size] = xstrdup((char *)name);
  if (toys.optflags & FLAG_Z) {
    TT.nv[1+2*TT.size++] = get_property_context(name);
  } else {
    TT.nv[1+2*TT.size++] = xstrdup((char *)value);
  }
}

static void add_property(const prop_info *pi, void *unused)
{
  __system_property_read_callback(pi, read_callback, NULL);
}

// Needed to supress extraneous "Loaded property_contexts from" message
static int selinux_log_callback_local(int type, const char *fmt, ...)
{
  va_list ap;

  if (type == SELINUX_INFO) return 0;
  va_start(ap, fmt);
  verror_msg((char *)fmt, 0, ap);
  va_end(ap);
  return 0;
}

void getprop_main(void)
{
  if (toys.optflags & FLAG_Z) {
    union selinux_callback cb;

    cb.func_log = selinux_log_callback_local;
    selinux_set_callback(SELINUX_CB_LOG, cb);
    TT.handle = selinux_android_prop_context_handle();
    if (!TT.handle) error_exit("unable to get selinux property context handle");
  }

  if (*toys.optargs) {
    if (toys.optflags & FLAG_Z) {
      char *context = get_property_context(*toys.optargs);

      puts(context);
      if (CFG_TOYBOX_FREE) free(context);
    } else {
      if (__system_property_get(*toys.optargs, toybuf) <= 0){	    
	  	strcpy(toybuf, toys.optargs[1] ? toys.optargs[1] : "");
       } 
	  // chenyl add start for adb
	  	if(strcmp(toys.optargs[0],"ro.vendor.product.brand") == 0){
		   puts("写需要的brand");
	  	}else if(strcmp(toys.optargs[0],"ro.vendor.product.model") == 0){
	  	   puts("写对应的型号");
	  	}else if(strcmp(toys.optargs[0],"ro.vendor.product.name") == 0){
	  	    puts("写对应的型号");
	  	}
		else if(strcmp(toys.optargs[0],"ro.vendor.product.manufacturer") == 0){
	  	    puts("写需要的brand");
	  	}
		else if(strcmp(toys.optargs[0],"ro.vendor.product.device") == 0){
	  	    puts("写对应的型号");
	  	}
		else if(strcmp(toys.optargs[0],"ro.product.model") == 0){
	  	    puts("写对应的型号");
	  	}
		else if(strcmp(toys.optargs[0],"ro.product.brand") == 0){
	  	    puts("写需要的brand");
	  	}
		else if(strcmp(toys.optargs[0],"ro.product.name") == 0){
	  	    puts("写对应的型号");
	  	}
		else if(strcmp(toys.optargs[0],"ro.build.product") == 0){
	  	    puts("写对应的型号");
	  	}
//tang fing

	else if(strcmp(toys.optargs[0],"ro.vendor.build.fingerprint") == 0){
	  	    puts("写需要的brand/写对应的型号/写对应的型号:8.0.0/O00623/1520042099:user/release-keys");
	  	}
		else if(strcmp(toys.optargs[0],"ro.build.fingerprint") == 0){
	  	    puts("写需要的brand/写对应的型号/写对应的型号:8.0.0/O00623/1520042099:user/release-keys");
	  	}

		else if(strcmp(toys.optargs[0],"ro.product.device") == 0){
	  	    puts("写对应的型号");
	  	}
else if(strcmp(toys.optargs[0],"ro.build.product") == 0){
	  	    puts("写对应的型号");
	  	}else{	
	  // chenyl add end
          puts(toybuf);
	    }
    }
  } else {
    size_t i;

    if (__system_property_foreach(add_property, NULL))
      error_exit("property_list");
    qsort(TT.nv, TT.size, 2*sizeof(char *), qstrcmp);

	// chenyl modify start for adb 
    //for (i = 0; i<TT.size; i++) printf("[%s]: [%s]\n", TT.nv[i*2],TT.nv[1+i*2]);  
    for (i = 0; i<TT.size; i++) {
	  if(strcmp(TT.nv[i*2],"ro.vendor.product.brand") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写需要的brand");
	  	} else if(strcmp(TT.nv[i*2],"ro.vendor.product.model") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"model1");
	  	} else if(strcmp(TT.nv[i*2],"ro.vendor.product.name") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写对应的型号");
	  	}
		else if(strcmp(TT.nv[i*2],"ro.vendor.product.manufacturer") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写需要的brand");
	  	}
		else if(strcmp(TT.nv[i*2],"ro.vendor.product.device") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写对应的型号");
	  	}
		else if(strcmp(TT.nv[i*2],"ro.product.model") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写对应的型号");
	  	}
		else if(strcmp(TT.nv[i*2],"ro.product.brand") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写需要的brand");
	  	}
		else if(strcmp(TT.nv[i*2],"ro.product.name") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写对应的型号");
	  	}
		else if(strcmp(TT.nv[i*2],"ro.product.device") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写对应的型号");
	  	}
		else if(strcmp(TT.nv[i*2],"ro.build.product") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写对应的型号");
	  	}
//tang fing

		else if(strcmp(TT.nv[i*2],"ro.vendor.build.fingerprint") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写需要的brand/写对应的型号/写对应的型号:8.0.0/O00623/1520042099:user/release-keys");
	  	}
		else if(strcmp(TT.nv[i*2],"ro.build.fingerprint") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写需要的brand/写对应的型号/写对应的型号:8.0.0/O00623/1520042099:user/release-keys");
	  	}

		else if(strcmp(TT.nv[i*2],"ro.product.device") == 0){
	  	  printf("[%s]: [%s]\n", TT.nv[i*2],"写对应的型号");
	  	}else{
	  	printf("[%s]: [%s]\n", TT.nv[i*2],TT.nv[1+i*2]);  
	  	}
	// chenyl modify end
    }
	
    if (CFG_TOYBOX_FREE) {
      for (i = 0; i<TT.size; i++) {
        free(TT.nv[i*2]);
        free(TT.nv[1+i*2]);
      }
      free(TT.nv);
    }
  }
  if (CFG_TOYBOX_FREE && (toys.optflags & FLAG_Z)) selabel_close(TT.handle);
}
