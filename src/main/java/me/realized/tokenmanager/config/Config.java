package me.realized.tokenmanager.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import me.realized.tokenmanager.TokenManagerPlugin;
import me.realized.tokenmanager.util.config.AbstractConfiguration;
import me.realized.tokenmanager.util.config.convert.Converter;
import org.bukkit.configuration.file.FileConfiguration;

public class Config extends AbstractConfiguration<TokenManagerPlugin> {

    @Getter
    private int version;
    @Getter
    private boolean checkForUpdates;
    @Getter
    private String onlineMode;
    @Getter
    private boolean altPrevention;
    @Getter
    private int defaultBalance;
    @Getter
    private long sendMin;
    @Getter
    private long sendMax;
    @Getter
    private boolean openSelectedEnabled;
    @Getter
    private String openSelectedShop;
    @Getter
    private String confirmPurchaseTitle;
    @Getter
    private String confirmPurchaseConfirm;
    @Getter
    private String confirmPurchaseCancel;
    @Getter
    private int clickDelay;
    @Getter
    private boolean checkInventoryFull;
    @Getter
    private boolean logPurchases;
    @Getter
    private boolean mysqlEnabled;
    @Getter
    private String mysqlUsername;
    @Getter
    private String mysqlPassword;
    @Getter
    private String mysqlHostname;
    @Getter
    private String mysqlPort;
    @Getter
    private String mysqlDatabase;
    @Getter
    private String mysqlTable;
    @Getter
    private String mysqlUrl;
    @Getter
    private boolean redisEnabled;
    @Getter
    private String redisServer;
    @Getter
    private int redisPort;
    @Getter
    private String redisPassword;
    @Getter
    private boolean registerEconomy;
    @Getter
    private int balanceTopUpdateInterval;

    public Config(final TokenManagerPlugin plugin) {
        super(plugin, "config");
    }

    @Override
    protected void loadValues(FileConfiguration configuration) throws Exception {
        if (!configuration.isInt("config-version")) {
            configuration = convert(new Converter2_3());
        } else if (configuration.getInt("config-version") < getLatestVersion()) {
            configuration = convert(null);
        }

        version = configuration.getInt("config-version");
        checkForUpdates = configuration.getBoolean("check-for-updates", true);
        onlineMode = configuration.getString("online-mode", "auto");
        altPrevention = configuration.getBoolean("alt-prevention", false);
        defaultBalance = configuration.getInt("default-balance", 25);
        sendMin = configuration.getInt("send-amount-limit.min", 1);
        sendMax = configuration.getInt("send-amount-limit.max", -1);
        openSelectedEnabled = configuration.getBoolean("shop.open-selected.enabled", false);
        openSelectedShop = configuration.getString("shop.open-selected.shop", "example").toLowerCase();
        confirmPurchaseTitle = configuration.getString("shop.confirm-purchase-gui.title", "Confirm Your Purchase");
        confirmPurchaseConfirm = configuration.getString("shop.confirm-purchase-gui.confirm-button", "STAINED_CLAY:5 1 name:&a&lBUY lore:&7Price:_&a%price%_tokens");
        confirmPurchaseCancel = configuration.getString("shop.confirm-purchase-gui.cancel-button", "STAINED_CLAY:14 1 name:&c&lCANCEL");
        clickDelay = configuration.getInt("shop.click-delay", 0);
        checkInventoryFull = configuration.getBoolean("shop.check-inventory-full", false);
        logPurchases = configuration.getBoolean("shop.log-purchases", false);
        mysqlEnabled = configuration.getBoolean("data.mysql.enabled", false);
        mysqlUsername = configuration.getString("data.mysql.username", "root");
        mysqlPassword = configuration.getString("data.mysql.password", "password");
        mysqlHostname = configuration.getString("data.mysql.hostname", "127.0.0.1");
        mysqlPort = configuration.getString("data.mysql.port", "3306");
        mysqlDatabase = configuration.getString("data.mysql.database", "database");
        mysqlTable = configuration.getString("data.mysql.table", "tokenmanager");
        mysqlUrl = configuration.getString("data.mysql.url", "jdbc:mysql://%hostname%:%port%/%database%");
        redisEnabled = configuration.getBoolean("data.mysql.redis.enabled", false);
        redisServer = configuration.getString("data.mysql.redis.server", "127.0.0.1");
        redisPort = configuration.getInt("data.mysql.redis.port", 6379);
        redisPassword = configuration.getString("data.mysql.redis.password", "");
        registerEconomy = configuration.getBoolean("data.register-economy", false);
        balanceTopUpdateInterval = configuration.getInt("data.balance-top-update-interval", 5);
    }

    private class Converter2_3 implements Converter {

        Converter2_3() {}

        @Override
        public Map<String, String> renamedKeys() {
            final Map<String, String> keys = new HashMap<>();
            keys.put("use-default.enabled", "shop.open-selected.enabled");
            keys.put("use-default.shop", "shop.open-selected.shop");
            keys.put("mysql.enabled", "data.mysql.enabled");
            keys.put("mysql.hostname", "data.mysql.hostname");
            keys.put("mysql.port", "data.mysql.port");
            keys.put("mysql.username", "data.mysql.username");
            keys.put("mysql.password", "data.mysql.password");
            keys.put("mysql.database", "data.mysql.database");
            keys.put("click-delay", "shop.click-delay");
            keys.put("update-balance-top", "data.balance-top-update-interval");
            keys.put("vault-hooks", "data.register-economy");
            return keys;
        }
    }
}
