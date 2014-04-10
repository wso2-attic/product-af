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
        ]
    });
};

