package it.flaten.mysql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySql {
    private final Map<String, List<MySqlPool>> pools = new HashMap<>();

    private MySqlPool createPool(String name, int id) {
        if (!this.pools.containsKey(name))
            this.pools.put(name, new ArrayList<MySqlPool>());

        MySqlPool pool = new MySqlPool(name + id);

        this.pools.get(name).set(id, pool);

        return pool;
    }

    public MySqlPool getPool(String name, int id) {
        if (!this.pools.containsKey(name))
            return this.createPool(name, id);

        if (this.pools.get(name).get(id) == null)
            return this.createPool(name, id);

        return this.pools.get(name).get(id);
    }

    public MySqlPool getPool(String name) {
        return this.getPool(name, 0);
    }

    public void close(String name) {
        for (MySqlPool pool : this.pools.get(name)) {
            pool.close();
        }
    }

    public void close() {
        for (String name : this.pools.keySet()) {
            this.close(name);
        }
    }
}
