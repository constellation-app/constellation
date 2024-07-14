# Autosave

Constellation has an automatic backup feature that periodically creates
a backup of your graph while you work (regardless of whether the graph
has ever been saved or not). By default, the autosave backup feature is
enabled and it is executed every 5 minutes. This can be adjusted via
Setup -> Options -> CONSTELLATION -> Application. Having this enabled 
allows you to recover your graph should something unexpected happen 
(e.g. computer crash, power outage) before you have the chance to save 
your graph.

When Constellation is restarted, the application will first check to see
if any of the autosaved backup files were associated with unsaved
graphs. If there are any files, the user will be asked whether they
would like to reload them. Once these files have been reloaded, the user
can continue to work on them as if nothing went wrong.
