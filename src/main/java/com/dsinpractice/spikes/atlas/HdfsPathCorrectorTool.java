package com.dsinpractice.spikes.atlas;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.conf.Configuration;

import java.io.File;
import java.net.URI;

public class HdfsPathCorrectorTool {

    static  private MiniDFSCluster cluster;
    static  private String hdfsURI;

    public static  void setUp() throws Exception {
//        super.setUp();
        Configuration conf = new Configuration();
        File baseDir = new File("./target/hdfs/").getAbsoluteFile();
        FileUtil.fullyDelete(baseDir);
        conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
        MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
        cluster = builder.build();
        hdfsURI = "hdfs://localhost:"+ cluster.getNameNodePort() + "/";
    }

    public void tearDown() throws Exception {
        cluster.shutdown();
    }

    public static void main(String[] args){
        HdfsPathCorrectorTool tool = new HdfsPathCorrectorTool();

        try {

            tool.setUp();


            DistributedFileSystem dfs = tool.cluster.getFileSystem();


            dfs.mkdirs(new Path("/path/to/schema"));
            FSDataOutputStream out=dfs.create(new Path("/path/to/schema/schema.avsc"));
            out.writeBytes("sssssss");
            out.close();



            verifyFilePath("/path/to/schemA/schema.avsc");



        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    public void testCreateLogEntry() throws Exception {
//        String logentry = new LogEntry().createLogEntry("TestStage", "TestCategory", "/testpath", cluster.getFileSystem());
//        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
////        assertTrue(logentry.startsWith(String.format("/testpath/TestStage_%s_", date)));
//    }


    static void verifyFilePath(String strPath){


            try {

                //String strPath = "/user/hive/NIxon/mittalsteelExt1.csv";

                String str[] = strPath.split("/");
                String incrementalPath = "";
                int i = 0;
                for( String str1 : str){

                    if( i == 0){
                        if (str1!=null && str1.length() == 0){
                            incrementalPath = "/";
                        } else {
                            incrementalPath = incrementalPath + verifyFilesPath("/", str1);
                        }
                    }
                    else
                    {
                        if(i < str.length-1){
                            incrementalPath = incrementalPath + verifyFilesPath(incrementalPath, str1) +"/";
                        }else{
                            incrementalPath = incrementalPath + verifyFilesPath(incrementalPath, str1);
                        }
                    }

                    i++;
                    System.out.println("incrementalPath " + incrementalPath );
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }


    static String verifyFilesPath(String basepath, String file) throws Exception{
        DistributedFileSystem dfs = cluster.getFileSystem();

        Path filepath = new Path(file);
        if (!dfs.exists(filepath)) {

            FileSystem fs = FileSystem.get(new Configuration());
            RemoteIterator<LocatedFileStatus> fileStatusListIterator =fs.listFiles(new Path(basepath), true);

            while(fileStatusListIterator.hasNext()){
                LocatedFileStatus fileStatus = fileStatusListIterator.next();
                //do stuff with the file like ...
                final Path path = fileStatus.getPath();//
                System.out.println("Actual path" + path);

                //              job.addFileToClassPath(fileStatus.getPath());
            }

            System.out.println("file does not exist");
        } else {
            System.out.println("file exist");
        }
        System.out.println(" path "+ basepath + " file " + file);
        return file;

    }



}
