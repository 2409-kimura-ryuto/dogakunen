//設定
function setting() {
  let settingLists = {
    heading: [
      1, //テーブルの見出し数(見出しを使用しない場合は「0」)
    ],
    number: [
      20, //テーブルに表示する最大のデータ件数(見出し数を除く)
    ],
    numberDisplayedPages: [
      5, //ページ番号の最大表示数（全部表示する場合は「false」）
    ],
    nextPage: [
      '次へ', //非表示にする場合は「''」(空文字)
    ],
    previousPage: [
      '前へ', //非表示にする場合は「''」(空文字)
    ],
  };
  return settingLists;
}

//ページ読み込み時に実行
let numberOfTableWrap = document.getElementsByClassName('TableWrap').length;
for (let i = 1; i <= numberOfTableWrap; i++) {
  var numberOfHeading = setting()['heading'][i - 1];
  var numberOfNumber = setting()['number'][i - 1];
  addTableWrapId(i);
  addPageNumWrapId(i)
  var tableWrapID = 'TableWrap' + i;
  var tableWrap = document.getElementById(tableWrapID);
  addTableRowId(i);
  createTable(i);
}

//クラス名「TableWrap」のtableタグにID名を追加
function addTableWrapId(i) {
  let tableWrapLists = document.getElementsByClassName('TableWrap');
  let tableWrap = tableWrapLists[i - 1];
  tableWrap.setAttribute('id', 'TableWrap' + i);
}

//クラス名「TableRow」のtrタグにID名を追加
function addTableRowId(i) {
  let tableWrapRowLists = tableWrap.getElementsByClassName('TableWrap_row');
  let numberOfTableRow = tableWrapRowLists.length;
  for (let j = 1; j <= numberOfTableRow - numberOfHeading; j++) {
    let tableWrapRow = tableWrapRowLists[j - 1 + numberOfHeading];
    tableWrapRow.setAttribute('id', 'TableWrap' + i + 'Row' + j);
  }
}

//クラス名「PageNumWrap」のdivタグにID名を追加
function addPageNumWrapId(i) {
  let pageNumWrapLists = document.getElementsByClassName('PageNumWrap');
  let pageNumWrap = pageNumWrapLists[i - 1];
  pageNumWrap.setAttribute('id', 'PageNumWrap' + i);
}

//表示用のテーブル作成とページ番号作成
function createTable(i) {
  let targetTable = tableWrap;
  let tableWrapRowLists = targetTable.getElementsByClassName('TableWrap_row');
  let pageRemainder = (tableWrapRowLists.length - numberOfHeading) % numberOfNumber;
  let pageQuotient = Math.floor((tableWrapRowLists.length - numberOfHeading) / numberOfNumber);
  let numberDisplayedPages = setting()['numberDisplayedPages'][i - 1];
  let pageHtml = '';
  let flag = true;

  if (pageRemainder === 0) {
    for (let pageNum = 1; pageNum <= pageQuotient; pageNum++) {
      if (pageNum === 1) {
        pageHtml += '<span class="PageNum -currentPage">' + pageNum + '</span>';
      } else {
        if (numberDisplayedPages) {
          if (pageNum <= numberDisplayedPages) {
            pageHtml += '<span class="PageNum" onclick="pagenation(' + i + ',' + pageNum + ');">' + pageNum + '</span>';
          } else if (flag) {
            pageHtml += '<span class="PageNum -dots">&middot;&middot;&middot;</span>';
            flag = false;
          }
        } else {
          pageHtml += '<span class="PageNum" onclick="pagenation(' + i + ',' + pageNum + ');">' + pageNum + '</span>';
        }
      }
    }
    if (pageQuotient > 1) {
      if (numberDisplayedPages) {
        if (flag) {
          pageHtml += '<span class="PageNum -nextPage" onclick="pagenation(' + i + ', 2);">' + setting()['nextPage'][i - 1] + '</span>';
        } else {
          pageHtml += '<span class="PageNum" onclick="pagenation(' + i + ',' + pageQuotient + ');">' + pageQuotient + '</span>\
                       <span class="PageNum -nextPage" onclick="pagenation(' + i + ', 2);">' + setting()['nextPage'][i - 1] + '</span>';
        }
      } else {
        pageHtml += '<span class="PageNum -nextPage" onclick="pagenation(' + i + ', 2);">' + setting()['nextPage'][i - 1] + '</span>';
      }
    }
  } else {
    for (let pageNum2 = 1; pageNum2 <= pageQuotient + 1; pageNum2++) {
      if (pageNum2 === 1) {
        pageHtml += '<span class="PageNum -currentPage">' + pageNum2 + '</span>';
      } else {
        if (numberDisplayedPages) {
          if (pageNum2 <= numberDisplayedPages) {
            pageHtml += '<span class="PageNum" onclick="pagenation(' + i + ',' + pageNum2 + ');">' + pageNum2 + '</span>';
          } else if (flag) {
            pageHtml += '<span class="PageNum -dots">&middot;&middot;&middot;</span>';
            flag = false;
          }
        } else {
          pageHtml += '<span class="PageNum" onclick="pagenation(' + i + ',' + pageNum2 + ');">' + pageNum2 + '</span>';
        }
      }
    }
    if (pageQuotient + 1 > 1) {
      if (numberDisplayedPages) {
        if (flag) {
          pageHtml += '<span class="PageNum -nextPage" onclick="pagenation(' + i + ', 2);"> ' + setting()['nextPage'][i - 1] + '</span>';
        } else {
          pageHtml += '<span class="PageNum" onclick="pagenation(' + i + ',' + (pageQuotient + 1) + ');">' + (pageQuotient + 1) + '</span>\
                       <span class="PageNum -nextPage" onclick="pagenation(' + i + ', 2);"> ' + setting()['nextPage'][i - 1] + '</span>';
        }
      } else {
        pageHtml += '<span class="PageNum -nextPage" onclick="pagenation(' + i + ', 2);"> ' + setting()['nextPage'][i - 1] + '</span>';
      }
    }
  }

  let pageNumWrapID = 'PageNumWrap' + i;
  document.getElementById(pageNumWrapID).innerHTML = pageHtml;

  //表示するのクラス名「TableWrap_row」調整
  for (let k = 1; k <= tableWrapRowLists.length - numberOfHeading; k++) {
    let TableWrapRowId = 'TableWrap' + i + 'Row' + k;
    if (k <= numberOfNumber) {
      document.getElementById(TableWrapRowId).style.display = '';
    } else {
      document.getElementById(TableWrapRowId).style.display = 'none';
    }
  }
}

//ページ番号をクリックした時の処理
function pagenation(tableNum, num) {
  let targetTableID = 'TableWrap' + tableNum;
  let targetTable = document.getElementById(targetTableID);
  let tableWrapRowLists = targetTable.getElementsByClassName('TableWrap_row');
  let heading = setting()['heading'][tableNum - 1];
  let number = setting()['number'][tableNum - 1];
  let numberDisplayedPages = setting()['numberDisplayedPages'][tableNum - 1];
  let leftPages = Math.ceil(numberDisplayedPages / 2);
  let rightPages = Math.floor(numberDisplayedPages / 2);
  let pageRemainder = (tableWrapRowLists.length - heading) % number;
  let pageQuotient = Math.floor((tableWrapRowLists.length - heading) / number);
  let pageHtml = '';
  let flag = true;

  //テーブルの行をいったん消す
  for (let i = 1; i <= tableWrapRowLists.length - heading; i++) {
    let TableWrapRowId = 'TableWrap' + tableNum + 'Row' + i;
    document.getElementById(TableWrapRowId).style.display = 'none';
  }

  //テーブルを再表示
  if (num <= pageQuotient || pageRemainder === 0) {
    for (let j = num * number - (number - 1); j <= num * number; j++) {
      let TableWrapRowId = 'TableWrap' + tableNum + 'Row' + j;
      document.getElementById(TableWrapRowId).style.display = '';
    }
  } else {
    for (let k = num * number - (number - 1); k <= num * number - number + pageRemainder; k++) {
      let TableWrapRowId = 'TableWrap' + tableNum + 'Row' + k;
      document.getElementById(TableWrapRowId).style.display = '';
    }
  }

  if (numberDisplayedPages) {
    if (num !== 1 && num > numberDisplayedPages - rightPages) {
      pageHtml += '<span class="PageNum -previousPage" onclick="pagenation(' + tableNum + ',' + (num - 1) + ');" > ' + setting()['previousPage'][tableNum - 1] + '</span>\
                   <span class="PageNum" onclick="pagenation(' + tableNum + ',1);">1</span>\
                   <span class="PageNum -dots">&middot;&middot;&middot;</span>';
    } else if (num !== 1) {
      pageHtml += '<span class="PageNum -previousPage" onclick="pagenation(' + tableNum + ',' + (num - 1) + ');" > ' + setting()['previousPage'][tableNum - 1] + '</span>';
    }
  } else {
    if (num !== 1) {
      pageHtml += '<span class="PageNum -previousPage" onclick="pagenation(' + tableNum + ',' + (num - 1) + ');" > ' + setting()['previousPage'][tableNum - 1] + '</span>';
    }
  }

  if (pageRemainder === 0) {
    for (let pageNum = 1; pageNum <= pageQuotient; pageNum++) {
      if (numberDisplayedPages) {
        if (num - leftPages < 1) {
          if (num === pageNum) {
            pageHtml += '<span class="PageNum -currentPage">' + pageNum + '</span>';
          } else {
            if (pageNum <= numberDisplayedPages) {
              pageHtml += '<span class="PageNum" onclick="pagenation(' + tableNum + ',' + pageNum + ');">' + pageNum + '</span>';
            }
          }
        } else {
          if ((pageNum > num - leftPages || pageNum > pageQuotient - numberDisplayedPages) && pageNum <= num + rightPages) {
            if (num === pageNum) {
              pageHtml += '<span class="PageNum -currentPage">' + pageNum + '</span>';
            } else {
              pageHtml += '<span class="PageNum" onclick="pagenation(' + tableNum + ',' + pageNum + ');">' + pageNum + '</span>';
            }
          }
        }
      } else {
        if (num === pageNum) {
          pageHtml += '<span class="PageNum -currentPage">' + pageNum + '</span>';
        } else {
          pageHtml += '<span class="PageNum" onclick="pagenation(' + tableNum + ',' + pageNum + ');">' + pageNum + '</span>';
        }
      }
    }

    if (numberDisplayedPages) {
      if (pageQuotient !== num && num < pageQuotient - rightPages) {
        pageHtml += '<span class="PageNum -dots">&middot;&middot;&middot;</span>\
                   <span class="PageNum" onclick="pagenation(' + tableNum + ',' + pageQuotient + ');">' + pageQuotient + '</span>\
                   <span class="PageNum -nextPage" onclick="pagenation(' + tableNum + ',' + (num + 1) + ');">' + setting()['nextPage'][tableNum - 1] + '</span>';
      } else if (pageQuotient !== num) {
        pageHtml += '<span class="PageNum -nextPage" onclick="pagenation(' + tableNum + ',' + (num + 1) + ');">' + setting()['nextPage'][tableNum - 1] + '</span>';
      }
    } else {
      if (pageQuotient !== num) {
        pageHtml += '<span class="PageNum -nextPage" onclick="pagenation(' + tableNum + ',' + (num + 1) + ');">' + setting()['nextPage'][tableNum - 1] + '</span>';
      }
    }

  } else {
    for (let pageNum2 = 1; pageNum2 <= pageQuotient + 1; pageNum2++) {
      if (numberDisplayedPages) {
        if (num - leftPages < 1) {
          if (num === pageNum2) {
            pageHtml += '<span class="PageNum -currentPage">' + pageNum2 + '</span>';
          } else {
            if (pageNum2 <= numberDisplayedPages) {
              pageHtml += '<span class="PageNum" onclick="pagenation(' + tableNum + ',' + pageNum2 + ');">' + pageNum2 + '</span>';
            }
          }
        } else {
          if ((pageNum2 > num - leftPages || pageNum2 > pageQuotient + 1 - numberDisplayedPages) && pageNum2 <= num + rightPages) {
            if (num === pageNum2) {
              pageHtml += '<span class="PageNum -currentPage">' + pageNum2 + '</span>';
            } else {
              pageHtml += '<span class="PageNum" onclick="pagenation(' + tableNum + ',' + pageNum2 + ');">' + pageNum2 + '</span>';
            }
          }
        }
      } else {
        if (num === pageNum2) {
          pageHtml += '<span class="PageNum -currentPage">' + pageNum2 + '</span>';
        } else {
          pageHtml += '<span class="PageNum" onclick="pagenation(' + tableNum + ',' + pageNum2 + ');">' + pageNum2 + '</span>';
        }
      }
    }

    if (numberDisplayedPages) {
      if (pageQuotient + 1 !== num && num < pageQuotient + 1 - rightPages) {
        pageHtml += '<span class="PageNum -dots">&middot;&middot;&middot;</span>\
                   <span class="PageNum" onclick="pagenation(' + tableNum + ',' + (pageQuotient + 1) + ');">' + (pageQuotient + 1) + '</span>\
                   <span class="PageNum -nextPage" onclick="pagenation(' + tableNum + ',' + (num + 1) + ');">' + setting()['nextPage'][tableNum - 1] + '</span>';
      } else if (pageQuotient + 1 !== num) {
        pageHtml += '<span class="PageNum -nextPage" onclick="pagenation(' + tableNum + ',' + (num + 1) + ');">' + setting()['nextPage'][tableNum - 1] + '</span>';
      }
    } else {
      if (pageQuotient + 1 !== num) {
        pageHtml += '<span class="PageNum -nextPage" onclick="pagenation(' + tableNum + ',' + (num + 1) + ');">' + setting()['nextPage'][tableNum - 1] + '</span>';
      }
    }
  }
  let pageNumWrapID = 'PageNumWrap' + tableNum;
  document.getElementById(pageNumWrapID).innerHTML = pageHtml;
}