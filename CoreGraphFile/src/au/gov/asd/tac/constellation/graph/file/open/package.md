<div>

The source files in this package have been copied/forked from
org.netbeans.modules.openfile. There are four reasons for this. (1) This
package is in the "User Utilities" module, which does not expose an API,
so we would need a dependency on the implementation. Bad. (2) There are
other things in the "User Utilities" module that we don't want. (3) We
need to change the source of FileChooser to make our filter have
precedence over the default filter. (4) We want to change the way that
the recent files list works. The default is to add files to the list
when a TC closes, and remove them when a TC opens. However, when the
application closes, the PROP_TC_CLOSED doesn't get fired, and the list
isn't updated. Instead, our change is to add the file to the list when
it is opened.

</div>
