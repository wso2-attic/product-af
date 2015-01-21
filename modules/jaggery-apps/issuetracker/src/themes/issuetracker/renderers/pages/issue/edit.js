var render = function (theme, data, meta, require) {
    theme('issue_', {
        title: [
            { partial:'title', context: data.title}
        ],
        header: [
            { partial:'header', context: data.header}
        ],
        body: [
            { partial:'editIssueBody', context: data.body}
        ],
        header_mid: [
            { partial:'headerMid' ,context: {isHome:false,title:"Edit Issue:  " + data.body.issue.summary, appkey:data.body.appkey}}
        ]
    });
};

