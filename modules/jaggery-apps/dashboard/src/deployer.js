var portalUrl = '/portal';

var repoPath = '/gadgets';

var username = 'admin';

var password = 'admin';

var rxtPath = '/gadgets/';

var lastUpdated = 0;

var addGadget = function (xml) {
    var name, provider, version, path,
        meta = new MetadataStore(username, password),
        gadget = meta.newResource(),
        ns = 'http://www.wso2.org/governance/metadata';
    provider = xml..ns::provider.text().toString();
    name = xml..ns::name.text().toString();
    version = xml..ns::version.text().toString();
    path = rxtPath + provider + '/' + name + '/' + version;
    gadget.content = xml.toXMLString();
    gadget.mediaType = 'application/vnd.wso2-gadget+xml';
    meta.put(path, gadget);
};

var buildRXT = function (name) {
    var rxt,
        path = portalUrl + repoPath + '/' + name + '/',
        thumbnail = path + 'thumbnail.jpg',
        url = path + name + '.xml';
    rxt = <metadata xmlns="http://www.wso2.org/governance/metadata">
        <gadget>
            <provider>{username}</provider>
            <name>{name}</name>
            <version>1.0</version>
            <thumbnail>{thumbnail}</thumbnail>
            <url>{url}</url>
            <status>CREATED</status>
        </gadget>
    </metadata>;
    return rxt;
};

var populate = function () {
    var i, name, length, gadgets, file,
        log = new Log(),
        repo = new File(repoPath);
    if (repo.isDirectory()) {
        gadgets = repo.listFiles();
        length = gadgets.length;
        for (i = 0; i < length; i++) {
            name = gadgets[i].getName();
            file = new File(repoPath + '/' + name + '/' + name + '.xml');
            if (file.getLastModified() > lastUpdated) {
                log.info('Deploying Gadget : ' + name);
                addGadget(buildRXT(name));
            }
        }
    }
    lastUpdated = new Date().getTime();
};

setInterval(populate, 10000);