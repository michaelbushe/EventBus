function addSocialBookMarkChild(parentDivId) {
    var parentDiv = document.getElementById(parentDivId);
    if (parentDiv) {
        var sociableSrc = getSocialSourceDiv();
        parentDiv.innerHTML = parentDiv.innerHTML+sociableSrc;
    }
}
function replaceInnerWithSocialBookMarks(parentDivId) {
    var parentDiv = document.getElementById(parentDivId);
    if (parentDiv) {
        var sociableSrc = getSocialSourceDiv();
        parentDiv.innerHTML = sociableSrc;
    }
}

function getSocialSourceDiv() {
    var sociableSrc = "<div class=\"sociable\">\n" +

       getSocialList() +
       "</div>";
    return sociableSrc;
}

function getSocialList() {
    var title = document.title;
    var url = document.URL;
    var result = "<div class=\"sociable_tagline\">\n" +
       "    <strong>Share:</strong>\n" +
       "    </div>\n" +
       "    <ul>\n" +
    "        <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://delicious.com/post?url="+url+"%2F&amp;title="+title+"&amp;\" title=\"del.icio.us\"><img src=\"http://www.eventbus.org/images/delicious.png\" title=\"del.icio.us\" alt=\"del.icio.us\" style=\"width: 16px; height: 16px;\" class=\"sociable-hovers\" /></a></li>\n" +
    "        <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.digg.com/submit?url="+url+"%2F&amp;title="+title+"\" title=\"Digg\"><img src=\"http://www.eventbus.org/images/digg.png\" title=\"Digg\" alt=\"Digg\" style=\"width: 16px; height: 16px;\" class=\"sociable-hovers\" /></a></li>\n" +
    "        <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.dzone.com/links/add.html?url="+url+"&amp;title="+title+"\" title=\"DZone\"><img src=\"http://www.eventbus.org/images/dzone.png\" title=\"DZone\" alt=\"DZone\" style=\"width: 16px; height: 16px;\" class=\"sociable-hovers\" /></a></li>\n" +
    "        <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://slashdot.org/bookmark.pl?title="+title+"&amp;url="+url+"%2F\" title=\"Slashdot\"><img src=\"http://www.eventbus.org/images/slashdot.png\" title=\"Slashdot\" alt=\"Slashdot\" style=\"width: 16px; height: 16px;\" class=\"sociable-hovers\" /></a></li>\n" +
    "        <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.facebook.com/share.php?u="+url+"&amp;t="+title+"\" title=\"Facebook\"><img src=\"http://www.eventbus.org/images/facebook.png\" title=\"Facebook\" alt=\"Facebook\" style=\"width: 16px; height: 16px;\" class=\"sociable-hovers\" /></a></li>\n" +
    "        <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.linkedin.com/shareArticle?mini=true&amp;url="+url+"%2F&amp;title="+title+"&amp;\" title=\"LinkedIn\"><img src=\"http://www.eventbus.org/images/linkedin.png\" title=\"LinkedIn\" alt=\"LinkedIn\" style=\"width: 16px; height: 16px;\" class=\"sociable-hovers\" /></a></li>\n" +
    "        <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://twitter.com/home?status="+title+"%20-%20"+url+"%2F\" title=\"Twitter\"><img src=\"http://www.eventbus.org/images/twitter.gif\" title=\"Twitter\" alt=\"Twitter\" style=\"width: 16px; height: 16px; \" class=\"sociable-hovers\" /></a></li>\n" +
    "        <li class=\"sociablelast\"><a rel=\"nofollow\"  target=\"_blank\" href=\"mailto:?subject="+title+"&amp;body="+url+"%2F\" title=\"email\"><img src=\"http://www.eventbus.org/images/email_link.png\" title=\"email\" alt=\"email\" style=\"width: 16px; height: 16px;\" class=\"sociable-hovers\" /></a></li>\n" +
    "    </li>\n" +
    "    </ul>\n" +
    "    </div>\n";
    return result;
}



