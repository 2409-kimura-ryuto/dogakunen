$(function() {
	// class='isStopped-button'が設定されたボタンをクリックしたらダイアログを表示する。
	$('.isStopped-button').on('click', function() {
		let result = confirm('アカウントを停止しますか');

		if (result) {
			return true;
		} else {
			return false;
		}
	});

	// class='revival-button'が設定されたボタンをクリックしたらダイアログを表示する。
    $('.revival-button').on('click', function() {
        let result = confirm('アカウントを復活しますか');

        if (result) {
            return true;
        } else {
            return false;
        }
    });
});
