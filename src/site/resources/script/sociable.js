//Javascript to dynamically add sociable bookmark links as a new child of a div
function addSocialBookToLogoDiv(parentDiv) {
}
function addSocialBookMarkChild(parentDivId) {
    var title = document.title;
    var docURL = document.URL;
    if (parentDiv) {
        var sociableSrc = "<div class=\"sociable\">\n" +
           "        <div class=\"sociable_tagline\">\n" +
           "        <strong>Share and Enjoy:</strong>\n" +
           "        </div>\n" +
           "        <ul>\n" +
           "            <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://delicious.com/post?url="+docURL+"%2F&amp;title="+title+"&amp;\" title=\"del.icio.us\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"del.icio.us\" alt=\"del.icio.us\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 0\" class=\"sociable-hovers\" /></a></li>\n" +
           "            <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.dzone.com/links/add.html?url="+docURL+"%2F&amp;title="+title+"\" title=\"DZone\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"DZone\" alt=\"DZone\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 -66px\" class=\"sociable-hovers\" /></a></li>\n" +
           "            <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://reddit.com/submit?url="+docURL+"%2F&amp;title="+title+"\" title=\"Reddit\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"Reddit\" alt=\"Reddit\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 -330px\" class=\"sociable-hovers\" /></a></li>\n" +
           "            <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://slashdot.org/bookmark.pl?title="+title+"&amp;url="+docURL+"%2F\" title=\"Slashdot\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"Slashdot\" alt=\"Slashdot\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 -398px\" class=\"sociable-hovers\" /></a></li>\n" +
           "            <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.stumbleupon.com/submit?url="+docURL+"%2F&amp;title="+title+"\" title=\"StumbleUpon\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"StumbleUpon\" alt=\"StumbleUpon\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 -464px\" class=\"sociable-hovers\" /></a></li>\n" +
           "            <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.facebook.com/share.php?u="+docURL+"%2F&amp;t="+title+"\" title=\"Facebook\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"Facebook\" alt=\"Facebook\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 -132px\" class=\"sociable-hovers\" /></a></li>\n" +
           "            <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.friendfeed.com/share?title="+title+"&amp;link="+docURL+"%2F\" title=\"FriendFeed\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"FriendFeed\" alt=\"FriendFeed\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 -198px\" class=\"sociable-hovers\" /></a></li>\n" +
           "            <li><a rel=\"nofollow\"  target=\"_blank\" href=\"http://www.linkedin.com/shareArticle?mini=true&amp;url="+docURL+"%2F&amp;title="+title+"&amp;\" title=\"LinkedIn\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"LinkedIn\" alt=\"LinkedIn\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 -264px\" class=\"sociable-hovers\" /></a></li>\n" +
           "            <li class=\"sociablelast\"><a rel=\"nofollow\"  target=\"_blank\" href=\"http://twitter.com/home?status="+title+"%20-%20"+docURL+"%2F\" title=\"Twitter\"><img src=\"http://www.eventbus.org/images/services-sprite.gif\" title=\"Twitter\" alt=\"Twitter\" style=\"width: 16px; height: 16px; background: transparent url(http://www.eventbus.org/images/services-sprite.gif) no-repeat; background-position:0 -530px\" class=\"sociable-hovers\" /></a></li>\n" +
           "        </li>\n" +
           "        </ul>\n" +
           "        </div>\n" +
           "    </div>";
        parentDiv.innerHTML = parentDiv.innerHTML+sociableSrc;
    }
}
