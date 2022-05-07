package com.example.team17zooseeker;

import android.content.Context;
import android.renderscript.Sampler;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.ToDoubleBiFunction;

@Database(entities = {edgeItem.class, nodeItem.class}, version = 1, exportSchema = false)
public abstract class ZooKeeperDatabase extends RoomDatabase {
    private static ZooKeeperDatabase singleton = null;

    public synchronized static ZooKeeperDatabase getSingleton(Context context) {

        if(singleton == null) {
            singleton = ZooKeeperDatabase.makeDatabase(context);
        }

        return singleton;

    }

    private static ZooKeeperDatabase makeDatabase(Context context) {

            return Room.databaseBuilder(context, ZooKeeperDatabase.class, "zooseeker_db")
                    .allowMainThreadQueries()
                    .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Executors.newSingleThreadScheduledExecutor().execute(() -> {

                                Map<String, nodeItem> nodes = null;
                                Map<String, edgeItem> edges = null;

                                try {
                                    nodes = nodeItem.loadNodeInfoJSON(context, "node.json");
                                    edges = edgeItem.loadEdgeInfoJSON(context, "edge.json");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                List<nodeItem> nodeList = new ArrayList<nodeItem>(nodes.values());
                                List<edgeItem> edgeList = new ArrayList<edgeItem>(edges.values());

                                getSingleton(context).nodeItemDao().insertAll(nodeList);
                                getSingleton(context).edgeItemDao().insertAll(edgeList);

                            });
                        }
                    })
                    .build();
    }

    public abstract EdgeItemDao edgeItemDao();
//    public abstract GraphItemDao graphItemDao();
    public abstract NodeItemDao nodeItemDao();
}
