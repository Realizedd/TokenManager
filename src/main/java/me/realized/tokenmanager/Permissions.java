package me.realized.tokenmanager;

public final class Permissions {

    private static final String PREFIX = "tokenmanager.";

    public static final String CMD_TOKEN = PREFIX + "use";
    public static final String CMD_ADMIN = PREFIX + "admin";
    public static final String CMD_BALANCE_OTHERS = PREFIX + "use.balance.others";
    public static final String CMD_SELL = PREFIX + "use.sell";
    public static final String CMD_SELL_ALL = PREFIX + "use.sell.all";
    public static final String CMD_SEND = PREFIX + "use.send";
    public static final String CMD_SHOP = PREFIX + "use.shop";
    public static final String CMD_TOP = PREFIX + "use.top";
    public static final String CMD_WORTH = PREFIX + "use.worth";
    public static final String CMD_WORTH_ALL = PREFIX + "use.worth.all";

    public static final String SHOP = PREFIX + "use.shop.";
    public static final String SHOP_SLOT_OLD = PREFIX + "use."; // TODO: Deprecated, remove support in future versions.

    private Permissions() {}
}
