var render = function (theme, data, meta, require) {
    theme('issue_', {
        title: [
			{ partial:'title', context: data.title}
		],
        header: [
            { partial:'header', context: data.header}
        ],
        header_mid: [
            { partial:'headerMid' ,context: {page:'home', appkey:data.body.appkey}}
        ]
    });
};

