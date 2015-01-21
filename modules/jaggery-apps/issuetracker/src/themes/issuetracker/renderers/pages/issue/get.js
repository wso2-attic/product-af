var render = function (theme, data, meta, require) {
    theme('issue_', {
        title: [
            { partial:'title', context: data.title}
        ],
        header: [
            { partial:'header', context: data.header}
        ],
        body: [
            { partial:'viewIssueBody', context: data.body}
        ],
        header_mid: [
            { partial:'headerMid' ,context: {isHome:false,title:data.body.data.issue.summary, appkey:data.body.appkey}}
        ]
    });
};

