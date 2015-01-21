var render = function (theme, data, meta, require) {
    theme('issue_', {
        title: [
			{ partial:'title', context: data.title}
		],
        header: [
            { partial:'header', context: data.header}
        ],
		body: [
			{ partial:'issueSearchBody', context: data.body}
		],
        header_mid: [
            { partial:'headerMid' ,context: {appkey:data.body.appkey}}
        ]
    });
};

