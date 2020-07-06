Defining Proxies
````````````````

Some plugins use HTTP or HTTPS to access data. The servers that offer resources via these protocols may be reachable directly from CONSTELLATION, or they may be behind a proxy server. This sections allows you to define whether or not a proxy is required to reach a particular server or collection of servers.

There are three sections. The first two sections (local hosts and per-suffix proxies) can use hostname suffixes to define a collection of servers. For example, the suffix "``.com``" matches hosts with the names "``maps.google.com``", "``facebook.com``", and so on.

Each name that begins with "." will match any hostname that ends with that name. Each name that does not begin with a "." must match the hostname exactly. All matches are done case-insensitively.

Proxies are specified using their hostname and port number, separated by ":".

If all sections are empty, then no proxy will be used.

Note that any changes to the proxy configuration require CONSTELLATION to be restarted.

Proxy selection
```````````````

The checkbox "Use defaults", when selected, indicates that the built-in defaults are used. (The built-in defaults may vary depending on the version of CONSTELLATION you are using.) To specify custom proxy settings, unselect this checkbox. When "Use defaults" is unselected, a proxy will be determined by looking at each of the following sections in order.

Local hosts
```````````

This comma-separated list defines hosts and collections of hosts that can be accessed directly. For example:

.. code-block:: text
  
              .my.org,google.com
  
This means that all hosts that end with ".my.org" (such as "server.my.org" and "fileshare.my.org", and the specific host "google.com", can be accessed directly.

Specific proxies
````````````````

if a hostname does not match the local hosts list, a specific proxy is looked for. This section contains a list of proxies specific to a name or suffix, one per line. For example:

.. code-block:: text
  
              .au = au-proxy:8080
              sydney.com = au-proxy:8080
              .com = other-proxy:8888
  
Any host ending with "``.au``", and the specific host "``sydney.com``", will use the proxy "``au-proxy``", port 8080. Any host ending with "``.com``" will use the proxy "``other-proxy``", port 8888.

Default proxy
`````````````

The proxy specified here will be used if a hostname does not match the local hosts list or a specific proxy. For example, if the exclusion list and the specific list are both empty, and the default proxy is "``myproxy:8080``", then all hosts will be accessed via the proxy "``myproxy``" using port 8080.

If a default proxy is not specified, then no proxy will be used.

----

Print on console
````````````````

This option prints debugging text on the CONSTELLATION console. It is useful to see which proxy is being selected for a particular host. Generally you won't want to do this.


.. help-id: au.gov.asd.tac.constellation.security.proxies
