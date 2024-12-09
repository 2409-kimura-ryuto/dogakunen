$(function() {
	// class='delete'が設定されたボタンをクリックしたらダイアログを表示する。
	$('.delete').on('click', function() {
		let result = confirm('削除しますか');

		if (result) {
			return true;
		} else {
			return false;
		}
	});
});