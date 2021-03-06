/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainPackage;

import BCLibrary.StoreBasic;
import Threads.ProgressThread;
import Threads.UpdateThread;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.json.JSONArray;
import org.json.JSONObject;

public class OrphanedCategories extends javax.swing.JInternalFrame {
    private StoreBasic store;
    private EscalationsDesktop desktop;
    private int categoryCount;
    private String currentTask;
    private int percentage;
    private int orphanedLength;
    private Connection database;
    private Statement statement;
    private PreparedStatement preppedStatement;
    private ProgressThread newThread;
    private UpdateThread apiLimitThread;
    
    public OrphanedCategories() {
        initComponents();
    }
    
    public OrphanedCategories(StoreBasic store, EscalationsDesktop desktop) throws SQLException {
        this.store = store;
        this.desktop = desktop;
        this.categoryCount = 0;
        this.percentage = 0;
        this.orphanedLength = 0;
        this.database = desktop.getDBConnection();
        this.database.setAutoCommit(false);
        this.statement = database.createStatement();
        this.preppedStatement = null;
        initComponents();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        orphanedCategoriesProgress = new javax.swing.JProgressBar();
        findOrphanedCats = new javax.swing.JButton();
        fixOrphanedCats = new javax.swing.JButton();
        resultsLabel = new javax.swing.JLabel();

        setClosable(true);
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Orphaned Categories");

        orphanedCategoriesProgress.setStringPainted(true);

        findOrphanedCats.setText("Check");
        findOrphanedCats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findOrphanedCatsActionPerformed(evt);
            }
        });

        fixOrphanedCats.setText("Fix");
        fixOrphanedCats.setEnabled(false);
        fixOrphanedCats.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fixOrphanedCatsActionPerformed(evt);
            }
        });

        resultsLabel.setText("Results posted here");
        resultsLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(orphanedCategoriesProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(resultsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(findOrphanedCats, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fixOrphanedCats, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(findOrphanedCats)
                        .addGap(17, 17, 17)
                        .addComponent(fixOrphanedCats))
                    .addComponent(resultsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7)
                .addComponent(orphanedCategoriesProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void findOrphanedCatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findOrphanedCatsActionPerformed
        SwingWorker <Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                //Set <Integer> categoryIDs = new TreeSet<>();
                int jsonArrayLength = 0;
                int page = 1;
                String results;
                JSONArray jsonArray;
                JSONObject jsonObject;
                fixOrphanedCats.setEnabled(false);
                findOrphanedCats.setEnabled(false);
                statement.executeUpdate("DELETE FROM allCats");
                database.setAutoCommit(false);
                try {
                //grab count of categories
                    store.get("categories/count");
                    jsonObject = new JSONObject(store.toString());
                    categoryCount = jsonObject.getInt("count");
                    newThread = new ProgressThread(orphanedCategoriesProgress, false, true);
                    apiLimitThread = new UpdateThread(desktop.getConnectivity(), store);
                    apiLimitThread.start();
                    newThread.start();
                } catch (IOException ex) {
                    Logger.getLogger(OrphanedCategories.class.getName()).log(Level.SEVERE, null, ex);
                }
                currentTask = "Gathering Categories";
                long startTimer = 0;
                int numRecords = 0;
                do {
                    try {
                            if (newThread.isTimerAvailable()) 
                                startTimer = System.nanoTime();
                        store.get("categories?limit=249&page=" + page);
                        results = store.toString();
                        jsonArray = new JSONArray(results);
                        jsonArrayLength = jsonArray.length();
                        String sqlStatement = "INSERT INTO allCats (catID, catName, parentID) " +
                                           "VALUES(?,?,?);";
                        preppedStatement = database.prepareStatement(sqlStatement);
                        for(int index = 0; index < jsonArrayLength; index++) {
                            jsonObject = jsonArray.getJSONObject(index);
                            numRecords++;
                            preppedStatement.setInt(1, jsonObject.getInt("id"));
                            preppedStatement.setString(2, jsonObject.getString("name"));
                            preppedStatement.setInt(3, jsonObject.getInt("parent_id"));
                            preppedStatement.executeUpdate();
                            percentage = (((index + 1 + (249 * (page - 1))) * 100) / categoryCount);
                        }
                        if (jsonArrayLength == 249) page++;
                        if (newThread.isTimerAvailable())
                            newThread.generateTime(startTimer, categoryCount, numRecords, 249);
                        newThread.setPercentage(percentage);
                    } catch (IOException ex) {
                        Logger.getLogger(OrphanedCategories.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } while (jsonArrayLength == 249);
                database.commit();
                percentage = 100;
                
                int orphanedParentLength = 0;
                ResultSet orphanedCats = statement.executeQuery("SELECT COUNT(catid) AS 'count' " +
                                                                "FROM allCats as 'child' " +
                                                                "WHERE parentid <> 0 AND NOT EXISTS " +
                                                                "(SELECT 1 FROM allCats as 'parent' " +
                                                                "WHERE child.parentid = parent.catid)");
                while(orphanedCats.next())
                    orphanedLength = orphanedCats.getInt("count");
                ResultSet distinctParentCats = statement.executeQuery("SELECT COUNT(DISTINCT(parentid)) AS 'count'" +
                                                                      "FROM allCats as 'child' " +
                                                                      "WHERE parentid <> 0 AND NOT EXISTS " +
                                                                      "(SELECT 1 FROM allCats as 'parent' " +
                                                                      "WHERE child.parentid = parent.catid)");                
                while(distinctParentCats.next())
                    orphanedParentLength = distinctParentCats.getInt("count");
                if (orphanedParentLength == 0)
                    resultsLabel.setText("No Orphaned categories found");
                else {
                    resultsLabel.setText("<html>Found " + orphanedParentLength + ((orphanedParentLength > 1) ? " parent categories<br>" : " parent category<br>")
                            + "Found " + orphanedLength + ((orphanedLength > 1) ? " categories</html>" : " category</html>"));
                    fixOrphanedCats.setEnabled(true);
                }
                findOrphanedCats.setEnabled(true);
                return null;
            }
        };
        worker.execute();
    }//GEN-LAST:event_findOrphanedCatsActionPerformed

    private void fixOrphanedCatsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fixOrphanedCatsActionPerformed
        SwingWorker <Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                fixOrphanedCats.setEnabled(false);
                //get or create Orphans category
                JSONObject newCat = new JSONObject().put("name", "Orphans");
                int orphanCatID = 0;
                newCat.put("is_visible", false);
                store.post("categories", newCat);
                String response = store.toString();
                response = response.replace("[", "");
                response = response.replace("]","");
                JSONObject res = new JSONObject(response);
                
                if (store.getStatusCode() == 409) {
                    orphanCatID = res.getJSONObject("details").getInt("duplicate_category");
                    store.put("categories/" + orphanCatID, "{\"is_visible\":false}");
                }
                else {
                    orphanCatID = res.getInt("id");
                }
                percentage = 0;
                newThread = new ProgressThread(orphanedCategoriesProgress, false, true);
                newThread.start();
                
                /*Set <Integer> keyset = orphanedCategories.getKeys();
                ArrayList <Category> values;*/
                int counter = 0;
                
                JSONObject orphanCategory = new JSONObject().put("parent_id", orphanCatID);
                currentTask = "Moving Orphaned Categories";
                ResultSet catIDs = statement.executeQuery("SELECT catid " +
                                                          "FROM allCats AS 'child' " +
                                                          "WHERE parentid <> 0 AND NOT EXISTS " +
                                                          "(SELECT 1 FROM allCats AS 'parent' " +
                                                          "WHERE child.parentid = parent.catid)");
                //we have to find the amount of rows returned
                long startTimer = 0;
                while (catIDs.next()) {
                    startTimer = System.nanoTime();
                    store.put("categories/" + catIDs.getInt("catID"), orphanCategory);
                    counter++;
                    percentage = counter * 100 / orphanedLength;
                    if (newThread.isTimerAvailable())
                        newThread.generateTime(startTimer, orphanedLength, counter, 1);
                    newThread.setPercentage(percentage);
                }
                return null;
            }
            
        };
        worker.execute();
    }//GEN-LAST:event_fixOrphanedCatsActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton findOrphanedCats;
    private javax.swing.JButton fixOrphanedCats;
    private javax.swing.JProgressBar orphanedCategoriesProgress;
    private javax.swing.JLabel resultsLabel;
    // End of variables declaration//GEN-END:variables
}
