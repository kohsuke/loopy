fs=new org.kohsuke.loopy.iso9660.ISO9660FileSystem(new File("/media/PORTABLE/ubuntu-8.04.2-server-i386.iso"),false)
fs.rootEntry.childEntries()
e=_.get("install") 
e
e.rr
f=e.get("netboot/pxelinux.0")
f.rr
f.symLinkTarget

