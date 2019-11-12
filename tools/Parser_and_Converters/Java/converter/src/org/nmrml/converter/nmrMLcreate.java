/*
 * $Id: Converter.java,v 1.0.alpha Feb 2014 (C) INRA - DJ $
 *
 * CC-BY 4.0
 */

package org.nmrml.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import java.util.*;
import java.lang.*;

import org.nmrml.schema.*;
import org.nmrml.parser.*;
import org.nmrml.parser.bruker.*;
import org.nmrml.parser.varian.*;
import org.nmrml.parser.jeol.*;

import org.nmrml.cv.*;
import org.nmrml.converter.*;

// create a nmrML file based on a java object tree generated by the JAXB XJC Tool (JDK 7 and above).
public class nmrMLcreate {

    private static final String nmrMLVersion = nmrMLversion.value;
    private static final String Version = "1.2.2";

    private enum Vendor_Type { bruker, varian, jeol; }

    public nmrMLcreate( ) { }

    public void launch( String[] args ) {

        Acqu2nmrML nmrmlObj = new Acqu2nmrML();

        /* Containers of Acquisition/Processing Parameters  */
        String Vendor = "bruker";

        Options options = new Options();
        options.addOption("h", "help", false, "prints the help content");
        options.addOption("v", "version", false, "prints the version");
        options.addOption("b","binary-data",false,"include fid binary data");
        options.addOption("z","compress",false,"compress binary data");
        options.addOption(OptionBuilder
           .withDescription("prints the nmrML XSD version")
           .withLongOpt("xsd-version")
           .create());
        options.addOption(OptionBuilder
           .withArgName("directory")
           .hasArg()
           .isRequired()
           .withDescription("input directory")
           .withLongOpt("inputdir")
           .create("i"));
        options.addOption(OptionBuilder
           .withArgName("config.properties")
           .hasArg()
           .withDescription("properties configuration file")
           .withLongOpt("prop")
           .create());
        options.addOption(OptionBuilder
           .withArgName("vendor")
           .hasArg()
           .withDescription("vendor type")
           .withLongOpt("type")
           .create("t"));
        options.addOption(OptionBuilder
           .withArgName("file")
           .hasArg()
           .withDescription("output file")
           .withLongOpt("outputfile")
           .create("o"));
        options.addOption(OptionBuilder
           .withArgName("string")
           .hasArg()
           .withDescription("raw spectrum identifier")
           .withLongOpt("acqidentifier")
           .create("a"));

        try {

           CommandLineParser parser = new GnuParser();
           CommandLine cmd = parser.parse(options, args);

        /* Properties object */
           Properties prop = new Properties();
           if (cmd.hasOption("prop")) {
               String conffile = cmd.getOptionValue("prop");
               prop.load(new FileInputStream(conffile));
           } else {
               prop.load(nmrMLpipe.class.getClassLoader().getResourceAsStream("resources/config.properties"));
           }
           String onto_ini = prop.getProperty("onto_ini_file");
           nmrmlObj.setSchemaLocation( prop.getProperty("schemaLocation") );

           CVLoader cvLoader = (new File(onto_ini)).isFile() ?
                          new CVLoader(new FileInputStream(onto_ini)) : 
                          new CVLoader(nmrMLpipe.class.getClassLoader().getResourceAsStream("resources/onto.ini"));
           nmrmlObj.setCVLoader(cvLoader);

        /* Vendor type */
           if(cmd.hasOption("t")) {
                 Vendor = cmd.getOptionValue("t").toLowerCase();
           }
           String vendor_ini = prop.getProperty(Vendor);
           Vendor_Type vendor_type = Vendor_Type.valueOf(Vendor);

       /* Vendor terms file */
           SpectrometerMapper vendorMapper = (new File(vendor_ini)).isFile() ? 
                         new SpectrometerMapper(vendor_ini) : 
                         vendor_type == Vendor_Type.bruker ? 
                               new SpectrometerMapper(nmrMLpipe.class.getClassLoader().getResourceAsStream("resources/bruker.ini")) :
                         vendor_type == Vendor_Type.varian ? 
                               new SpectrometerMapper(nmrMLpipe.class.getClassLoader().getResourceAsStream("resources/varian.ini")) :
                               new SpectrometerMapper(nmrMLpipe.class.getClassLoader().getResourceAsStream("resources/jeol.ini")) ;

           nmrmlObj.setVendorMapper(vendorMapper);

        /* Input */
           String inputFolder = cmd.getOptionValue("i");
           if (vendor_type != Vendor_Type.jeol) {
               inputFolder = ( inputFolder.lastIndexOf("/") == inputFolder.length() ) ? inputFolder : inputFolder.concat("/");
           }
           nmrmlObj.setInputFolder(inputFolder);

       /* set Acquisition Identifier if specified */
           if (cmd.hasOption("acqidentifier")) {
               nmrmlObj.setAcqIdentifier(cmd.getOptionValue("acqidentifier"));
           }

       /* Get Acquisition Parameters depending on the vendor type */
       /* Bruker & Varian */
           if (vendor_type == Vendor_Type.bruker || vendor_type == Vendor_Type.varian) {
              File dataFolder = new File(inputFolder);
              String acqFstr = dataFolder.getAbsolutePath() + "/" + vendorMapper.getTerm("FILES", "ACQUISITION_FILE");
              File acquFile = new File(acqFstr);
              if(acquFile.isFile() && acquFile.canRead()) {
                  switch (vendor_type) {
                     case bruker:
                          BrukerAcquReader brukerAcqObj = new BrukerAcquReader(acquFile);
                          nmrmlObj.setAcqu(brukerAcqObj.read());
                          break;
                     case varian:
                          VarianAcquReader varianAcqObj = new VarianAcquReader(acquFile);
                          Acqu acq = varianAcqObj.read();
                          acq.setSoftware(vendorMapper.getTerm("SOFTWARE", "SOFTWARE"));
                          acq.setSoftVersion(vendorMapper.getTerm("SOFTWARE", "VERSION"));
                          switch (Integer.parseInt(vendorMapper.getTerm("BYTORDA", "ENDIAN"))){
                               case 0 :
                                   acq.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                                   break;
                               case 1 :
                                   acq.setByteOrder(ByteOrder.BIG_ENDIAN);
                                   break;
                               default:
                                   break;
                          }
                          acq.setDecoupledNucleus("off");
                          nmrmlObj.setAcqu(acq);
                          break;
                  }
              }
              else {
                  System.err.println("ACQUISITION_FILE not available or readable: " + acqFstr);
                  System.exit(1);
              }
           }
       /* Jeol */
           if (vendor_type == Vendor_Type.jeol) {
               JeolAcquReader jeolAcqObj = new JeolAcquReader(inputFolder);
               jeolAcqObj.setVendorMapper(vendorMapper);
               Acqu acq = jeolAcqObj.read();
               nmrmlObj.setAcqu(acq);
           }

           nmrmlObj.setVendorLabel(Vendor.toUpperCase());
           nmrmlObj.setIfbinarydata(cmd.hasOption("b"));
           nmrmlObj.setCompressed(cmd.hasOption("z"));

           if(cmd.hasOption("o")) {
                nmrmlObj.Convert2nmrML( cmd.getOptionValue("o","output.nmrML") );
           } else {
                nmrmlObj.Convert2nmrML( null );
           }

        } catch(MissingOptionException e){
            boolean help = false;
            boolean version = false;
            boolean xsdversion = false;
            try{
              Options helpOptions = new Options();
              helpOptions.addOption("h", "help", false, "prints the help content");
              helpOptions.addOption("v", "version", false, "prints the version");
              helpOptions.addOption(OptionBuilder.withDescription("prints the nmrML XSD version").withLongOpt("xsd-version").create());
              CommandLineParser parser = new PosixParser();
              CommandLine line = parser.parse(helpOptions, args);
              if(line.hasOption("h")) help = true;
              if(line.hasOption("v")) version = true;
              if(line.hasOption("xsd-version")) xsdversion = true;
            } catch(Exception ex){ }
            if(!help && !version && !xsdversion) System.err.println(e.getMessage());
            if (help) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp( "nmrMLcreate" , options );
            }
            if (version) {
                System.out.println("nmrML Create version = " + Version);
            }
            if (xsdversion) {
                System.out.println("nmrML XSD version = " + nmrMLVersion);
            }
            System.exit(1);
        } catch(MissingArgumentException e){
            System.err.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "App" , options );
            System.exit(1);
        } catch(ParseException e){
            System.err.println("Error while parsing the command line: "+e.getMessage());
            System.exit(1);
        } catch( Exception e ) {
            e.printStackTrace();
        }

    }
}
