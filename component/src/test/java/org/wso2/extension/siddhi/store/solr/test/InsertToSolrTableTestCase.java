/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.store.solr.test;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.stream.input.InputHandler;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.store.solr.exceptions.SolrClientServiceException;
import org.wso2.extension.siddhi.store.solr.impl.SolrClientServiceImpl;

/**
 * This test class contains the test cases related to inserting the events to solr event table
 */
public class InsertToSolrTableTestCase {
    @Test
    public void insertEventsToSolrEventTable() throws InterruptedException, SolrClientServiceException {
        SiddhiManager siddhiManager = new SiddhiManager();
        SolrClientServiceImpl indexerService = SolrClientServiceImpl.INSTANCE;
        String defineQuery =
                "define stream FooStream (time long, date string);" +
                "@store(type='solr', url='localhost:9983', collection='TEST2', base.config='gettingstarted', " +
                "shards='2', replicas='2', schema='time long stored, date string stored', commit.async='true') " +
                "define table FooTable(time long, date string);";
        String insertQuery = "" +
                             "@info(name = 'query1') " +
                             "from FooStream   " +
                             "insert into FooTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(defineQuery + insertQuery);
        InputHandler fooTable = siddhiAppRuntime.getInputHandler("FooStream");
        try {
            siddhiAppRuntime.start();
            fooTable.send(new Object[]{45324211L, "1970-03-01 23:34:34 456"});
            fooTable.send(new Object[]{Long.MIN_VALUE, "2016-03-01 23:34:34 456"});
            fooTable.send(new Object[]{Long.MAX_VALUE, "2005-03-01 23:34:34 456"});
        } catch (Exception e) {
            //ignored
        } finally {
            indexerService.deleteCollection("TEST2");
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void insertEventsToSolrEventTableWithPrimaryKeys() throws InterruptedException, SolrClientServiceException {
        SiddhiManager siddhiManager = new SiddhiManager();
        SolrClientServiceImpl indexerService = SolrClientServiceImpl.INSTANCE;
        String defineQuery =
                "define stream FooStream (firstname string, lastname string, age int);" +
                "@PrimaryKey('firstname','lastname')" +
                "@store(type='solr', url='localhost:9983', collection='TEST3', base.config='gettingstarted', " +
                "shards='2', replicas='2', schema='recordId string stored, lastname string stored, age int stored', " +
                "commit.async='false')" +
                "define table FooTable(recordId string, lastname string, age int);";
        String insertQuery = "" +
                             "@info(name = 'query1') " +
                             "from FooStream   " +
                             "select firstname as recordId, lastname, age " +
                             "insert into FooTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(defineQuery + insertQuery);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        simulateEvents(indexerService, siddhiAppRuntime, fooStream);
    }

    private void simulateEvents(SolrClientServiceImpl indexerService, SiddhiAppRuntime siddhiAppRuntime,
                                InputHandler fooStream) throws SolrClientServiceException {
        try {
            siddhiAppRuntime.start();
            fooStream.send(new Object[]{"first1", "last1", 23});
            fooStream.send(new Object[]{"first2", "last2", 45});
            fooStream.send(new Object[]{"first1", "last1", 100});
        } catch (Exception e) {
            //ignored
        } finally {
            indexerService.deleteCollection("TEST3");
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void insertEventsToSolrEventTableWithPrimaryKeys2() throws InterruptedException, SolrClientServiceException {
        SiddhiManager siddhiManager = new SiddhiManager();
        SolrClientServiceImpl indexerService = SolrClientServiceImpl.INSTANCE;
        String defineQuery =
                "define stream FooStream (firstname string, lastname string, age int);" +
                "@PrimaryKey('someOtherfield','anotherField')" +
                "@store(type='solr', url='localhost:9983', collection='TEST3', base.config='gettingstarted', " +
                "shards='2', replicas='2', schema='recordId string stored, lastname string stored, age int stored', " +
                "commit.async='true')" +
                "define table FooTable(recordId string, lastname string, age int);";
        String insertQuery = "" +
                             "@info(name = 'query1') " +
                             "from FooStream   " +
                             "select firstname as recordId, lastname, age " +
                             "insert into FooTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(defineQuery + insertQuery);
        InputHandler fooStream = siddhiAppRuntime.getInputHandler("FooStream");
        simulateEvents(indexerService, siddhiAppRuntime, fooStream);
    }

    @Test
    public void insertEventsToSolrEventTableNoCommitAsync() throws InterruptedException, SolrClientServiceException {
        SiddhiManager siddhiManager = new SiddhiManager();
        SolrClientServiceImpl indexerService = SolrClientServiceImpl.INSTANCE;
        String defineQuery =
                "define stream FooStream (time long, date string);" +
                "@store(type='solr', url='localhost:9983', collection='X', base.config='gettingstarted', " +
                "shards='2', replicas='2', schema='time long stored, date string stored') " +
                "define table FooTable(time long, date string);";
        String insertQuery = "" +
                             "@info(name = 'query1') " +
                             "from FooStream   " +
                             "insert into FooTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(defineQuery + insertQuery);
        InputHandler fooTable = siddhiAppRuntime.getInputHandler("FooStream");
        simulateEvents2(indexerService, siddhiAppRuntime, fooTable);
    }

    private void simulateEvents2(SolrClientServiceImpl indexerService, SiddhiAppRuntime siddhiAppRuntime,
                                 InputHandler fooTable) throws SolrClientServiceException {
        try {
            siddhiAppRuntime.start();
            fooTable.send(new Object[]{45324211L, "1970-03-01 23:34:34 456"});
            fooTable.send(new Object[]{Long.MIN_VALUE, "2016-03-01 23:34:34 456"});
            fooTable.send(new Object[]{Long.MAX_VALUE, "2005-03-01 23:34:34 456"});
        } catch (Exception e) {
            //ignored
        } finally {
            indexerService.deleteCollection("X");
            siddhiAppRuntime.shutdown();
        }
    }

    @Test
    public void insertEventsToSolrEventTableNoCommitAsync2() throws InterruptedException, SolrClientServiceException {
        SiddhiManager siddhiManager = new SiddhiManager();
        SolrClientServiceImpl indexerService = SolrClientServiceImpl.INSTANCE;
        String defineQuery =
                "define stream FooStream (time long, date string);" +
                "@store(type='solr', url='localhost:9983', collection='X', base.config='gettingstarted', " +
                "shards='2', replicas='2', schema='time long stored, date string stored', commit.async='') " +
                "define table FooTable(time long, date string);";
        String insertQuery = "" +
                             "@info(name = 'query1') " +
                             "from FooStream   " +
                             "insert into FooTable ;";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(defineQuery + insertQuery);
        InputHandler fooTable = siddhiAppRuntime.getInputHandler("FooStream");
        simulateEvents2(indexerService, siddhiAppRuntime, fooTable);
    }
}
