package me.realized.tm.data;

public enum Action {

    BALANCE,
    ADD,
    REMOVE,
    EXISTS,

    TABLE("CREATE TABLE IF NOT EXISTS tokenmanager (uuid varchar(36) NOT NULL, tokens bigint(255) NOT NULL, PRIMARY KEY (uuid)) ENGINE=InnoDB DEFAULT CHARSET=latin1"), // Checks for table on the db.
    GET("SELECT * FROM tokenmanager WHERE uuid=\"{0}\""), // Returns player's balance.
    CREATE("INSERT INTO tokenmanager (uuid, tokens) VALUES (\"{0}\", {1})"), // Generates player's data on the db.
    TOP("SELECT * FROM tokenmanager ORDER BY tokens DESC LIMIT 10"), // Returns top tokens.
    SET("UPDATE tokenmanager SET tokens={1} WHERE uuid=\"{0}\""); // Sets player's token balance.

    private String query;

    Action() {}

    Action(String query) {
        this.query = query;
    }

    public String query(Object... replacers) {
        String result = query;

        for (int i = 0; i < replacers.length; i++) {
            if (i + 1 < replacers.length) {
                result = result.replace(replacers[i].toString(), replacers[++i].toString());
            }
        }

        return result;
    }
}
