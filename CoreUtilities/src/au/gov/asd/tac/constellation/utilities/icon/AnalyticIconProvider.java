/*
 * Copyright 2010-2024 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.utilities.icon;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * An IconProvider defining icons which might be used for analysis purposes.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = ConstellationIconProvider.class)
public class AnalyticIconProvider implements ConstellationIconProvider {

    private static final String CODE_NAME_BASE = "au.gov.asd.tac.constellation.utilities";

    private static final String COMMUNICATIONS_CATEGORY = "Communications";
    private static final String DOCUMENT_CATEGORY = "Document";
    private static final String INTERNET_CATEGORY = "Internet";
    private static final String MISCELLANEOUS_CATEGORY = "Miscellaneous";
    private static final String NETWORK_CATEGORY = "Network";
    private static final String PERSON_CATEGORY = "Person";
    private static final String SECURITY_CATEGORY = "Security";
    private static final String STRUCTURE_CATEGORY = "Structure";
    private static final String TRANSPORT_CATEGORY = "Transport";

    public static final ConstellationIcon CALL = new ConstellationIcon.Builder("Call", new FileIconData("modules/ext/icons/call.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon CELL_TOWER = new ConstellationIcon.Builder("Cell Tower", new FileIconData("modules/ext/icons/cell_tower.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon CHAT = new ConstellationIcon.Builder("Chat", new FileIconData("modules/ext/icons/chat.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon EMAIL = new ConstellationIcon.Builder("Email", new FileIconData("modules/ext/icons/email.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon GROUP_CHAT = new ConstellationIcon.Builder("Group Chat", new FileIconData("modules/ext/icons/group_chat.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon PHONE = new ConstellationIcon.Builder("Phone", new FileIconData("modules/ext/icons/phone.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon SIM_CARD = new ConstellationIcon.Builder("SIM Card", new FileIconData("modules/ext/icons/sim_card.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon SIP_CALL = new ConstellationIcon.Builder("SIP Call", new FileIconData("modules/ext/icons/sip_call.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon TABLET = new ConstellationIcon.Builder("Tablet", new FileIconData("modules/ext/icons/tablet.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();
    public static final ConstellationIcon VIDEO_CHAT = new ConstellationIcon.Builder("Video Chat", new FileIconData("modules/ext/icons/video_chat.png", CODE_NAME_BASE))
            .addCategory(COMMUNICATIONS_CATEGORY)
            .build();

    public static final ConstellationIcon IDENTIFICATION = new ConstellationIcon.Builder("Identification", new FileIconData("modules/ext/icons/identification.png", CODE_NAME_BASE))
            .addCategory(DOCUMENT_CATEGORY)
            .build();
    public static final ConstellationIcon PAPERCLIP = new ConstellationIcon.Builder("Paperclip", new FileIconData("modules/ext/icons/paperclip.png", CODE_NAME_BASE))
            .addCategory(DOCUMENT_CATEGORY)
            .build();
    public static final ConstellationIcon PASSPORT = new ConstellationIcon.Builder("Passport", new FileIconData("modules/ext/icons/passport.png", CODE_NAME_BASE))
            .addCategory(DOCUMENT_CATEGORY)
            .build();

    public static final ConstellationIcon AIM = new ConstellationIcon.Builder("Aim", new FileIconData("modules/ext/icons/aim.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon AIRBNB = new ConstellationIcon.Builder("Airbnb", new FileIconData("modules/ext/icons/airbnb.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon AMAZON = new ConstellationIcon.Builder("Amazon", new FileIconData("modules/ext/icons/amazon.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon ANDROID = new ConstellationIcon.Builder("Android", new FileIconData("modules/ext/icons/android.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon AOL = new ConstellationIcon.Builder("AOL", new FileIconData("modules/ext/icons/aol.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon APPLE = new ConstellationIcon.Builder("Apple", new FileIconData("modules/ext/icons/apple.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BAIDU = new ConstellationIcon.Builder("Baidu", new FileIconData("modules/ext/icons/baidu.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BANKIN = new ConstellationIcon.Builder("Bankin", new FileIconData("modules/ext/icons/bankin.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BEHANCE = new ConstellationIcon.Builder("Behance", new FileIconData("modules/ext/icons/behance.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BING = new ConstellationIcon.Builder("Bing", new FileIconData("modules/ext/icons/bing.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BITLY = new ConstellationIcon.Builder("Bitly", new FileIconData("modules/ext/icons/bitly.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BITTORRENT = new ConstellationIcon.Builder("Bittorrent", new FileIconData("modules/ext/icons/bittorrent.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BLACKBERRY = new ConstellationIcon.Builder("Blackberry", new FileIconData("modules/ext/icons/blackberry.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BLENDER = new ConstellationIcon.Builder("Blender", new FileIconData("modules/ext/icons/blender.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon BLOGGER = new ConstellationIcon.Builder("Blogger", new FileIconData("modules/ext/icons/blogger.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon CHROME = new ConstellationIcon.Builder("Chrome", new FileIconData("modules/ext/icons/chrome.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon CODEPEN = new ConstellationIcon.Builder("Codepen", new FileIconData("modules/ext/icons/codepen.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon DAILYMOTION = new ConstellationIcon.Builder("Dailymotion", new FileIconData("modules/ext/icons/dailymotion.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon DEVIANTART = new ConstellationIcon.Builder("Deviantart", new FileIconData("modules/ext/icons/deviantart.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon DRIBBBLE = new ConstellationIcon.Builder("Dribbble", new FileIconData("modules/ext/icons/dribbble.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon DRIVE = new ConstellationIcon.Builder("Drive", new FileIconData("modules/ext/icons/drive.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon DROPBOX = new ConstellationIcon.Builder("Dropbox", new FileIconData("modules/ext/icons/dropbox.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon EBAY = new ConstellationIcon.Builder("Ebay", new FileIconData("modules/ext/icons/ebay.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon ENVATO = new ConstellationIcon.Builder("Envato", new FileIconData("modules/ext/icons/envato.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon EVERNOTE = new ConstellationIcon.Builder("Evernote", new FileIconData("modules/ext/icons/evernote.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon FACEBOOK = new ConstellationIcon.Builder("Facebook", new FileIconData("modules/ext/icons/facebook.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon FANCY = new ConstellationIcon.Builder("Fancy", new FileIconData("modules/ext/icons/fancy.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon FEEDLY = new ConstellationIcon.Builder("Feedly", new FileIconData("modules/ext/icons/feedly.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon FIREFOX = new ConstellationIcon.Builder("Firefox", new FileIconData("modules/ext/icons/firefox.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon FIVE_HUNDRED_PX = new ConstellationIcon.Builder("500px", new FileIconData("modules/ext/icons/five_hundred_px.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon FLICKR = new ConstellationIcon.Builder("Flickr", new FileIconData("modules/ext/icons/flickr.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon FOURSQUARE = new ConstellationIcon.Builder("Foursquare", new FileIconData("modules/ext/icons/foursquare.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon GITHUB = new ConstellationIcon.Builder("Github", new FileIconData("modules/ext/icons/github.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon GMAIL = new ConstellationIcon.Builder("Gmail", new FileIconData("modules/ext/icons/gmail.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon GOOGLE = new ConstellationIcon.Builder("Google", new FileIconData("modules/ext/icons/google.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon GOOGLE_PLUS = new ConstellationIcon.Builder("Google+", new FileIconData("modules/ext/icons/google_plus.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon HANGOUTS = new ConstellationIcon.Builder("Hangouts", new FileIconData("modules/ext/icons/hangouts.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon ICQ = new ConstellationIcon.Builder("ICQ", new FileIconData("modules/ext/icons/icq.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon IMDB = new ConstellationIcon.Builder("IMDB", new FileIconData("modules/ext/icons/imdb.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon IMGUR = new ConstellationIcon.Builder("Imgur", new FileIconData("modules/ext/icons/imgur.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon INSTAGRAM = new ConstellationIcon.Builder("Instagram", new FileIconData("modules/ext/icons/instagram.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon INTERNET_EXPLORER = new ConstellationIcon.Builder("Internet Explorer", new FileIconData("modules/ext/icons/internet_explorer.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon INVISION = new ConstellationIcon.Builder("Invision", new FileIconData("modules/ext/icons/invision.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon JABBER = new ConstellationIcon.Builder("Jabber", new FileIconData("modules/ext/icons/jabber.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon KAKAO_TALK = new ConstellationIcon.Builder("Kakao Talk", new FileIconData("modules/ext/icons/kakao_talk.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon KIK = new ConstellationIcon.Builder("Kik", new FileIconData("modules/ext/icons/kik.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon LASTFM = new ConstellationIcon.Builder("Lastfm", new FileIconData("modules/ext/icons/lastfm.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon LINKEDIN = new ConstellationIcon.Builder("Linkedin", new FileIconData("modules/ext/icons/linkedin.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon MAGENTO = new ConstellationIcon.Builder("Magento", new FileIconData("modules/ext/icons/magento.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon MEDIUM = new ConstellationIcon.Builder("Medium", new FileIconData("modules/ext/icons/medium.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon MESSENGER = new ConstellationIcon.Builder("Messenger", new FileIconData("modules/ext/icons/messenger.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon MSN = new ConstellationIcon.Builder("MSN", new FileIconData("modules/ext/icons/msn.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon NAVER = new ConstellationIcon.Builder("Naver", new FileIconData("modules/ext/icons/naver.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon NETFLIX = new ConstellationIcon.Builder("Netflix", new FileIconData("modules/ext/icons/netflix.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon OFFICE = new ConstellationIcon.Builder("Office", new FileIconData("modules/ext/icons/office.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon OPENID = new ConstellationIcon.Builder("OpenID", new FileIconData("modules/ext/icons/openid.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon OPERA = new ConstellationIcon.Builder("Opera", new FileIconData("modules/ext/icons/opera.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon OUTLOOK = new ConstellationIcon.Builder("Outlook", new FileIconData("modules/ext/icons/outlook.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PANDORA = new ConstellationIcon.Builder("Pandora", new FileIconData("modules/ext/icons/pandora.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PASTEBIN = new ConstellationIcon.Builder("Pastebin", new FileIconData("modules/ext/icons/pastebin.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PAYPAL = new ConstellationIcon.Builder("Paypal", new FileIconData("modules/ext/icons/paypal.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PERISCOPE = new ConstellationIcon.Builder("Periscope", new FileIconData("modules/ext/icons/periscope.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PHOTOSHOP = new ConstellationIcon.Builder("Photoshop", new FileIconData("modules/ext/icons/photoshop.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PICASA = new ConstellationIcon.Builder("Picasa", new FileIconData("modules/ext/icons/picasa.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PINTEREST = new ConstellationIcon.Builder("Pinterest", new FileIconData("modules/ext/icons/pinterest.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon POCKET = new ConstellationIcon.Builder("Pocket", new FileIconData("modules/ext/icons/pocket.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PRINCIPLE = new ConstellationIcon.Builder("Principle", new FileIconData("modules/ext/icons/principle.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon PRODUCT_HUNT = new ConstellationIcon.Builder("Product Hunt", new FileIconData("modules/ext/icons/product_hunt.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon QQ = new ConstellationIcon.Builder("QQ", new FileIconData("modules/ext/icons/qq.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon RDIO = new ConstellationIcon.Builder("Rdio", new FileIconData("modules/ext/icons/rdio.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon REDDIT = new ConstellationIcon.Builder("Reddit", new FileIconData("modules/ext/icons/reddit.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon RSS = new ConstellationIcon.Builder("RSS", new FileIconData("modules/ext/icons/rss.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SAFARI = new ConstellationIcon.Builder("Safari", new FileIconData("modules/ext/icons/safari.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SCOOPIT = new ConstellationIcon.Builder("Scoopit", new FileIconData("modules/ext/icons/scoopit.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SHOPIFY = new ConstellationIcon.Builder("Shopify", new FileIconData("modules/ext/icons/shopify.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SINA_WEIBO = new ConstellationIcon.Builder("Sina Weibo", new FileIconData("modules/ext/icons/sina_weibo.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SKETCH = new ConstellationIcon.Builder("Sketch", new FileIconData("modules/ext/icons/sketch.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SKYPE = new ConstellationIcon.Builder("Skype", new FileIconData("modules/ext/icons/skype.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SLACK = new ConstellationIcon.Builder("Slack", new FileIconData("modules/ext/icons/slack.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SLASHDOT = new ConstellationIcon.Builder("Slashdot", new FileIconData("modules/ext/icons/slashdot.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SNAPCHAT = new ConstellationIcon.Builder("Snapchat", new FileIconData("modules/ext/icons/snapchat.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SOUNDCLOUD = new ConstellationIcon.Builder("Soundcloud", new FileIconData("modules/ext/icons/soundcloud.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SPOTIFY = new ConstellationIcon.Builder("Spotify", new FileIconData("modules/ext/icons/spotify.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon STACKOVERFLOW = new ConstellationIcon.Builder("Stackoverflow", new FileIconData("modules/ext/icons/stackoverflow.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon SURESPOT = new ConstellationIcon.Builder("Surespot", new FileIconData("modules/ext/icons/surespot.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon TALKBOX = new ConstellationIcon.Builder("Talkbox", new FileIconData("modules/ext/icons/talkbox.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon TANGO = new ConstellationIcon.Builder("Tango", new FileIconData("modules/ext/icons/tango.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon TELEGRAM = new ConstellationIcon.Builder("Telegram", new FileIconData("modules/ext/icons/telegram.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon TINDER = new ConstellationIcon.Builder("Tinder", new FileIconData("modules/ext/icons/tinder.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon TRELLO = new ConstellationIcon.Builder("Trello", new FileIconData("modules/ext/icons/trello.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon TUMBLR = new ConstellationIcon.Builder("Tumblr", new FileIconData("modules/ext/icons/tumblr.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon TWITCH = new ConstellationIcon.Builder("Twitch", new FileIconData("modules/ext/icons/twitch.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon TWITTER = new ConstellationIcon.Builder("Twitter", new FileIconData("modules/ext/icons/twitter.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon VIADEO = new ConstellationIcon.Builder("Viadeo", new FileIconData("modules/ext/icons/viadeo.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon VIBER = new ConstellationIcon.Builder("Viber", new FileIconData("modules/ext/icons/viber.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon VIMEO = new ConstellationIcon.Builder("Vimeo", new FileIconData("modules/ext/icons/vimeo.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon VINE = new ConstellationIcon.Builder("Vine", new FileIconData("modules/ext/icons/vine.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon VK = new ConstellationIcon.Builder("VK", new FileIconData("modules/ext/icons/vk.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon WEIXIN = new ConstellationIcon.Builder("Weixin", new FileIconData("modules/ext/icons/weixin.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon WHATSAPP = new ConstellationIcon.Builder("Whatsapp", new FileIconData("modules/ext/icons/whatsapp.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon WIKIPEDIA = new ConstellationIcon.Builder("Wikipedia", new FileIconData("modules/ext/icons/wikipedia.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon WORDPRESS = new ConstellationIcon.Builder("Wordpress", new FileIconData("modules/ext/icons/wordpress.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon YAHOO = new ConstellationIcon.Builder("Yahoo", new FileIconData("modules/ext/icons/yahoo.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon YELP = new ConstellationIcon.Builder("Yelp", new FileIconData("modules/ext/icons/yelp.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon YOUTUBE = new ConstellationIcon.Builder("Youtube", new FileIconData("modules/ext/icons/youtube.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();
    public static final ConstellationIcon ZELLO = new ConstellationIcon.Builder("Zello", new FileIconData("modules/ext/icons/zello.png", CODE_NAME_BASE))
            .addCategory(INTERNET_CATEGORY)
            .build();

    public static final ConstellationIcon BOMB = new ConstellationIcon.Builder("Bomb", new FileIconData("modules/ext/icons/bomb.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon CAMERA = new ConstellationIcon.Builder("Camera", new FileIconData("modules/ext/icons/camera.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon CHART = new ConstellationIcon.Builder("Chart", new FileIconData("modules/ext/icons/chart.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon CLOCK = new ConstellationIcon.Builder("Clock", new FileIconData("modules/ext/icons/clock.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon CLOUD = new ConstellationIcon.Builder("Cloud", new FileIconData("modules/ext/icons/cloud.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon DALEK = new ConstellationIcon.Builder("Dalek", new FileIconData("modules/ext/icons/dalek.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon FALSE = new ConstellationIcon.Builder("False", new FileIconData("modules/ext/icons/false.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .addAlias("false")
            .build();
    public static final ConstellationIcon FLAME = new ConstellationIcon.Builder("Flame", new FileIconData("modules/ext/icons/flame.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon FINGERPRINT = new ConstellationIcon.Builder("Fingerprint", new FileIconData("modules/ext/icons/fingerprint.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon GALAXY = new ConstellationIcon.Builder("Galaxy", new FileIconData("modules/ext/icons/galaxy.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon GLOBE = new ConstellationIcon.Builder("Globe", new FileIconData("modules/ext/icons/globe.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon GRAPH = new ConstellationIcon.Builder("Graph", new FileIconData("modules/ext/icons/graph.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon HAL_9000 = new ConstellationIcon.Builder("HAL-9000", new FileIconData("modules/ext/icons/hal-9000.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon HEART = new ConstellationIcon.Builder("Heart", new FileIconData("modules/ext/icons/heart.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon INFINITY = new ConstellationIcon.Builder("Infinity", new FileIconData("modules/ext/icons/infinity.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon INVADER = new ConstellationIcon.Builder("Invader", new FileIconData("modules/ext/icons/invader.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon KEY = new ConstellationIcon.Builder("Key", new FileIconData("modules/ext/icons/key.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon LOCK = new ConstellationIcon.Builder("Lock", new FileIconData("modules/ext/icons/lock.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon LIGHTNING = new ConstellationIcon.Builder("Lightning", new FileIconData("modules/ext/icons/lightning.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon MAP = new ConstellationIcon.Builder("Map", new FileIconData("modules/ext/icons/map.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon MARKER = new ConstellationIcon.Builder("Marker", new FileIconData("modules/ext/icons/marker.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon MR_SQUIGGLE = new ConstellationIcon.Builder("Mr Squiggle", new FileIconData("modules/ext/icons/mr_squiggle.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon MUSIC = new ConstellationIcon.Builder("Music", new FileIconData("modules/ext/icons/music.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon PUZZLE = new ConstellationIcon.Builder("Puzzle", new FileIconData("modules/ext/icons/puzzle.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon SHIELD = new ConstellationIcon.Builder("Shield", new FileIconData("modules/ext/icons/shield.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon SIGNAL = new ConstellationIcon.Builder("Signal", new FileIconData("modules/ext/icons/signal.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon SNOWFLAKE = new ConstellationIcon.Builder("Snowflake", new FileIconData("modules/ext/icons/snowflake.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon STAR = new ConstellationIcon.Builder("Star", new FileIconData("modules/ext/icons/star.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .build();
    public static final ConstellationIcon TRUE = new ConstellationIcon.Builder("True", new FileIconData("modules/ext/icons/true.png", CODE_NAME_BASE))
            .addCategory(MISCELLANEOUS_CATEGORY)
            .addAlias("true")
            .build();

    public static final ConstellationIcon BLUETOOTH = new ConstellationIcon.Builder("Bluetooth", new FileIconData("modules/ext/icons/bluetooth.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon COOKIE = new ConstellationIcon.Builder("Cookie", new FileIconData("modules/ext/icons/cookie.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon COMPACT_DISK = new ConstellationIcon.Builder("Compact Disk", new FileIconData("modules/ext/icons/compact_disk.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon DESKTOP = new ConstellationIcon.Builder("Desktop", new FileIconData("modules/ext/icons/desktop.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon FLOPPY_DISK = new ConstellationIcon.Builder("Floppy Disk", new FileIconData("modules/ext/icons/floppy_disk.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon GRAPHICS_PROCESSING_UNIT = new ConstellationIcon.Builder("Graphics Processing Unit", new FileIconData("modules/ext/icons/graphics_processing_unit.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon HEADSET = new ConstellationIcon.Builder("Headset", new FileIconData("modules/ext/icons/headset.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon INTERNET = new ConstellationIcon.Builder("Internet", new FileIconData("modules/ext/icons/internet.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon KEYBOARD = new ConstellationIcon.Builder("Keyboard", new FileIconData("modules/ext/icons/keyboard.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon LAPTOP = new ConstellationIcon.Builder("Laptop", new FileIconData("modules/ext/icons/laptop.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon LINUX = new ConstellationIcon.Builder("Linux", new FileIconData("modules/ext/icons/linux.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon MALWARE = new ConstellationIcon.Builder("Malware", new FileIconData("modules/ext/icons/malware.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon MICROPROCESSOR = new ConstellationIcon.Builder("Microprocessor", new FileIconData("modules/ext/icons/microprocessor.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon MOUSE = new ConstellationIcon.Builder("Mouse", new FileIconData("modules/ext/icons/mouse.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon NETWORK = new ConstellationIcon.Builder("Network", new FileIconData("modules/ext/icons/network.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon NETWORK_INTERFACE_CARD = new ConstellationIcon.Builder("Network Interface Card", new FileIconData("modules/ext/icons/network_interface_card.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon OSX = new ConstellationIcon.Builder("OSX", new FileIconData("modules/ext/icons/osx.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon PRINTER = new ConstellationIcon.Builder("Printer", new FileIconData("modules/ext/icons/printer.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon ROUTER = new ConstellationIcon.Builder("Router", new FileIconData("modules/ext/icons/router.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon SD_CARD = new ConstellationIcon.Builder("SD Card", new FileIconData("modules/ext/icons/sd_card.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon SERVER = new ConstellationIcon.Builder("Server", new FileIconData("modules/ext/icons/server.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon SPEAKER = new ConstellationIcon.Builder("Speaker", new FileIconData("modules/ext/icons/speaker.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon UNIFORM_RESOURCE_LOCATOR = new ConstellationIcon.Builder("Uniform Resource Locator", new FileIconData("modules/ext/icons/uniform_resource_locator.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon UNIVERSAL_SERIAL_BUS = new ConstellationIcon.Builder("Universal Serial Bus", new FileIconData("modules/ext/icons/universal_serial_bus.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon WEBCAM = new ConstellationIcon.Builder("Webcam", new FileIconData("modules/ext/icons/webcam.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();
    public static final ConstellationIcon WINDOWS = new ConstellationIcon.Builder("Windows", new FileIconData("modules/ext/icons/windows.png", CODE_NAME_BASE))
            .addCategory(NETWORK_CATEGORY)
            .build();

    public static final ConstellationIcon GROUP = new ConstellationIcon.Builder("Group", new FileIconData("modules/ext/icons/group.png", CODE_NAME_BASE))
            .addCategory(PERSON_CATEGORY)
            .build();
    public static final ConstellationIcon PERSON = new ConstellationIcon.Builder("Person", new FileIconData("modules/ext/icons/person.png", CODE_NAME_BASE))
            .addCategory(PERSON_CATEGORY)
            .build();

    public static final ConstellationIcon MD5 = new ConstellationIcon.Builder("MD5", new FileIconData("modules/ext/icons/md5.png", CODE_NAME_BASE))
            .addCategory(SECURITY_CATEGORY)
            .build();
    public static final ConstellationIcon SHA1 = new ConstellationIcon.Builder("SHA1", new FileIconData("modules/ext/icons/sha1.png", CODE_NAME_BASE))
            .addCategory(SECURITY_CATEGORY)
            .build();
    public static final ConstellationIcon SHA256 = new ConstellationIcon.Builder("SHA256", new FileIconData("modules/ext/icons/sha256.png", CODE_NAME_BASE))
            .addCategory(SECURITY_CATEGORY)
            .build();

    public static final ConstellationIcon CITY = new ConstellationIcon.Builder("City", new FileIconData("modules/ext/icons/city.png", CODE_NAME_BASE))
            .addCategory(STRUCTURE_CATEGORY)
            .build();
    public static final ConstellationIcon BUILDING = new ConstellationIcon.Builder("Building", new FileIconData("modules/ext/icons/building.png", CODE_NAME_BASE))
            .addCategory(STRUCTURE_CATEGORY)
            .build();
    public static final ConstellationIcon FACTORY = new ConstellationIcon.Builder("Factory", new FileIconData("modules/ext/icons/factory.png", CODE_NAME_BASE))
            .addCategory(STRUCTURE_CATEGORY)
            .build();
    public static final ConstellationIcon HOUSE = new ConstellationIcon.Builder("House", new FileIconData("modules/ext/icons/house.png", CODE_NAME_BASE))
            .addCategory(STRUCTURE_CATEGORY)
            .build();

    public static final ConstellationIcon BIKE = new ConstellationIcon.Builder("Bike", new FileIconData("modules/ext/icons/bike.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon BOAT = new ConstellationIcon.Builder("Boat", new FileIconData("modules/ext/icons/boat.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon BUS = new ConstellationIcon.Builder("Bus", new FileIconData("modules/ext/icons/bus.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon CAR = new ConstellationIcon.Builder("Car", new FileIconData("modules/ext/icons/car.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon PLANE = new ConstellationIcon.Builder("Plane", new FileIconData("modules/ext/icons/plane.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon RUN = new ConstellationIcon.Builder("Run", new FileIconData("modules/ext/icons/run.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon TARDIS = new ConstellationIcon.Builder("Tardis", new FileIconData("modules/ext/icons/tardis.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon TRAIN = new ConstellationIcon.Builder("Train", new FileIconData("modules/ext/icons/train.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon TRAM = new ConstellationIcon.Builder("Tram", new FileIconData("modules/ext/icons/tram.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();
    public static final ConstellationIcon WALK = new ConstellationIcon.Builder("Walk", new FileIconData("modules/ext/icons/walk.png", CODE_NAME_BASE))
            .addCategory(TRANSPORT_CATEGORY)
            .build();

    @Override
    public List<ConstellationIcon> getIcons() {
        final List<ConstellationIcon> analyticIcons = new ArrayList<>();
        analyticIcons.add(CALL);
        analyticIcons.add(CELL_TOWER);
        analyticIcons.add(CHAT);
        analyticIcons.add(EMAIL);
        analyticIcons.add(GROUP_CHAT);
        analyticIcons.add(PHONE);
        analyticIcons.add(SIM_CARD);
        analyticIcons.add(SIP_CALL);
        analyticIcons.add(TABLET);
        analyticIcons.add(VIDEO_CHAT);

        analyticIcons.add(IDENTIFICATION);
        analyticIcons.add(PAPERCLIP);
        analyticIcons.add(PASSPORT);

        analyticIcons.add(AIM);
        analyticIcons.add(AIRBNB);
        analyticIcons.add(AMAZON);
        analyticIcons.add(ANDROID);
        analyticIcons.add(AOL);
        analyticIcons.add(APPLE);
        analyticIcons.add(BAIDU);
        analyticIcons.add(BANKIN);
        analyticIcons.add(BEHANCE);
        analyticIcons.add(BITLY);
        analyticIcons.add(BING);
        analyticIcons.add(BITTORRENT);
        analyticIcons.add(BLACKBERRY);
        analyticIcons.add(BLENDER);
        analyticIcons.add(BLOGGER);
        analyticIcons.add(CHROME);
        analyticIcons.add(CODEPEN);
        analyticIcons.add(DAILYMOTION);
        analyticIcons.add(DEVIANTART);
        analyticIcons.add(DRIBBBLE);
        analyticIcons.add(DRIVE);
        analyticIcons.add(DROPBOX);
        analyticIcons.add(EBAY);
        analyticIcons.add(ENVATO);
        analyticIcons.add(EVERNOTE);
        analyticIcons.add(FACEBOOK);
        analyticIcons.add(FANCY);
        analyticIcons.add(FEEDLY);
        analyticIcons.add(FIREFOX);
        analyticIcons.add(FIVE_HUNDRED_PX);
        analyticIcons.add(FLICKR);
        analyticIcons.add(FOURSQUARE);
        analyticIcons.add(GITHUB);
        analyticIcons.add(GMAIL);
        analyticIcons.add(GOOGLE);
        analyticIcons.add(GOOGLE_PLUS);
        analyticIcons.add(HANGOUTS);
        analyticIcons.add(ICQ);
        analyticIcons.add(IMDB);
        analyticIcons.add(IMGUR);
        analyticIcons.add(INSTAGRAM);
        analyticIcons.add(INTERNET_EXPLORER);
        analyticIcons.add(INVISION);
        analyticIcons.add(JABBER);
        analyticIcons.add(KAKAO_TALK);
        analyticIcons.add(KIK);
        analyticIcons.add(LASTFM);
        analyticIcons.add(LINKEDIN);
        analyticIcons.add(MAGENTO);
        analyticIcons.add(MEDIUM);
        analyticIcons.add(MESSENGER);
        analyticIcons.add(MSN);
        analyticIcons.add(NAVER);
        analyticIcons.add(NETFLIX);
        analyticIcons.add(OFFICE);
        analyticIcons.add(OPENID);
        analyticIcons.add(OPERA);
        analyticIcons.add(OUTLOOK);
        analyticIcons.add(PANDORA);
        analyticIcons.add(PASTEBIN);
        analyticIcons.add(PAYPAL);
        analyticIcons.add(PERISCOPE);
        analyticIcons.add(PHOTOSHOP);
        analyticIcons.add(PICASA);
        analyticIcons.add(PINTEREST);
        analyticIcons.add(POCKET);
        analyticIcons.add(PRINCIPLE);
        analyticIcons.add(PRODUCT_HUNT);
        analyticIcons.add(QQ);
        analyticIcons.add(RDIO);
        analyticIcons.add(REDDIT);
        analyticIcons.add(RSS);
        analyticIcons.add(SAFARI);
        analyticIcons.add(SCOOPIT);
        analyticIcons.add(SHOPIFY);
        analyticIcons.add(SINA_WEIBO);
        analyticIcons.add(SKETCH);
        analyticIcons.add(SKYPE);
        analyticIcons.add(SLACK);
        analyticIcons.add(SLASHDOT);
        analyticIcons.add(SNAPCHAT);
        analyticIcons.add(SOUNDCLOUD);
        analyticIcons.add(SPOTIFY);
        analyticIcons.add(STACKOVERFLOW);
        analyticIcons.add(SURESPOT);
        analyticIcons.add(TALKBOX);
        analyticIcons.add(TANGO);
        analyticIcons.add(TELEGRAM);
        analyticIcons.add(TINDER);
        analyticIcons.add(TRELLO);
        analyticIcons.add(TUMBLR);
        analyticIcons.add(TWITCH);
        analyticIcons.add(TWITTER);
        analyticIcons.add(VIADEO);
        analyticIcons.add(VIBER);
        analyticIcons.add(VIMEO);
        analyticIcons.add(VINE);
        analyticIcons.add(VK);
        analyticIcons.add(WEIXIN);
        analyticIcons.add(WHATSAPP);
        analyticIcons.add(WIKIPEDIA);
        analyticIcons.add(WORDPRESS);
        analyticIcons.add(YAHOO);
        analyticIcons.add(YELP);
        analyticIcons.add(YOUTUBE);
        analyticIcons.add(ZELLO);

        analyticIcons.add(BOMB);
        analyticIcons.add(CAMERA);
        analyticIcons.add(CHART);
        analyticIcons.add(CLOCK);
        analyticIcons.add(CLOUD);
        analyticIcons.add(DALEK);
        analyticIcons.add(FALSE);
        analyticIcons.add(FLAME);
        analyticIcons.add(FINGERPRINT);
        analyticIcons.add(GALAXY);
        analyticIcons.add(GLOBE);
        analyticIcons.add(GRAPH);
        analyticIcons.add(HAL_9000);
        analyticIcons.add(HEART);
        analyticIcons.add(INFINITY);
        analyticIcons.add(INVADER);
        analyticIcons.add(KEY);
        analyticIcons.add(LOCK);
        analyticIcons.add(LIGHTNING);
        analyticIcons.add(MAP);
        analyticIcons.add(MARKER);
        analyticIcons.add(MR_SQUIGGLE);
        analyticIcons.add(MUSIC);
        analyticIcons.add(PUZZLE);
        analyticIcons.add(SHIELD);
        analyticIcons.add(SIGNAL);
        analyticIcons.add(SNOWFLAKE);
        analyticIcons.add(STAR);
        analyticIcons.add(TRUE);

        analyticIcons.add(BLUETOOTH);
        analyticIcons.add(COOKIE);
        analyticIcons.add(COMPACT_DISK);
        analyticIcons.add(DESKTOP);
        analyticIcons.add(FLOPPY_DISK);
        analyticIcons.add(GRAPHICS_PROCESSING_UNIT);
        analyticIcons.add(HEADSET);
        analyticIcons.add(INTERNET);
        analyticIcons.add(KEYBOARD);
        analyticIcons.add(LAPTOP);
        analyticIcons.add(LINUX);
        analyticIcons.add(MALWARE);
        analyticIcons.add(MICROPROCESSOR);
        analyticIcons.add(MOUSE);
        analyticIcons.add(NETWORK);
        analyticIcons.add(NETWORK_INTERFACE_CARD);
        analyticIcons.add(OSX);
        analyticIcons.add(PRINTER);
        analyticIcons.add(ROUTER);
        analyticIcons.add(SD_CARD);
        analyticIcons.add(SERVER);
        analyticIcons.add(SPEAKER);
        analyticIcons.add(UNIFORM_RESOURCE_LOCATOR);
        analyticIcons.add(UNIVERSAL_SERIAL_BUS);
        analyticIcons.add(WEBCAM);
        analyticIcons.add(WINDOWS);

        analyticIcons.add(MD5);
        analyticIcons.add(SHA1);
        analyticIcons.add(SHA256);

        analyticIcons.add(GROUP);
        analyticIcons.add(PERSON);

        analyticIcons.add(CITY);
        analyticIcons.add(BUILDING);
        analyticIcons.add(FACTORY);
        analyticIcons.add(HOUSE);

        analyticIcons.add(BIKE);
        analyticIcons.add(BOAT);
        analyticIcons.add(BUS);
        analyticIcons.add(CAR);
        analyticIcons.add(PLANE);
        analyticIcons.add(RUN);
        analyticIcons.add(TARDIS);
        analyticIcons.add(TRAIN);
        analyticIcons.add(TRAM);
        analyticIcons.add(WALK);

        return analyticIcons;
    }
}
