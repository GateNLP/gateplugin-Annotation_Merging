/*
 *  TestLearningAPI.java
 * 
 *  Yaoyong Li 08/10/2007
 *
 *  $Id: TestAnnotationMergingPlugin.java, v 1.0 2007-10-08 11:44:16 +0000 yaoyong $
 */
package gate.merger.test;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.GateConstants;
import gate.merger.AnnotationMergingMain;
import gate.merger.MergingMethodsEnum;
import gate.test.GATEPluginTestCase;

public class TestAnnotationMergingPlugin extends GATEPluginTestCase {
  
  /** The test for AnnotationMerging. */
  public void testAnnotationMergingPlugin() throws Exception {

    Boolean savedSpaceSetting = Gate.getUserConfig().getBoolean(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME);
    Gate.getUserConfig().put(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME, Boolean.FALSE);
    //Create a object for merging
    AnnotationMergingMain mergerOne = (AnnotationMergingMain)Factory
      .createResource("gate.merger.AnnotationMergingMain");
    //A corpus    
    Corpus corpus = Factory.newCorpus("DataSet");
    Document doc = Factory.newDocument(this.getClass().getResource("/beijing-opera.xml"), "UTF-8");
    System.out.println(this.getClass().getResource("beijing-opera.xml"));
    corpus.add(doc);
    

    gate.creole.SerialAnalyserController controller;
    controller = (gate.creole.SerialAnalyserController)Factory
      .createResource("gate.creole.SerialAnalyserController");
    controller.setCorpus(corpus);
    controller.add(mergerOne);
    mergerOne.setAnnSetOutput("mergerAnns");
    mergerOne.setAnnSetsForMerging("ann1;ann2;ann3");
    mergerOne.setAnnTypesAndFeats("sent->Op;Os");
    MergingMethodsEnum methodMerger = MergingMethodsEnum.MajorityVoting;
    mergerOne.setMergingMethod(methodMerger);
    controller.execute();
    
    AnnotationSet anns = doc.getAnnotations("mergerAnns").get("sent");
    int num = obtainAnns(anns, "Op", "true");
    assertEquals(num, 5);
    anns = doc.getAnnotations("mergerAnns").get("Os");
    num = anns.size();
    assertEquals(num, 2);
    doc.removeAnnotationSet("mergerAnns");

    methodMerger = MergingMethodsEnum.MergingByAnnotatorNum;
    mergerOne.setMergingMethod(methodMerger);
    mergerOne.setMinimalAnnNum("3");
    controller.execute();
    doc = corpus.get(0);
    anns = doc.getAnnotations("mergerAnns").get("sent");
    num = obtainAnns(anns, "Op", "true");
    assertEquals(num, 4);
    anns = doc.getAnnotations("mergerAnns").get("Os");
    num = anns.size();
    assertEquals(num, 2);
    System.out.println("completed");
    corpus.clear();
    Factory.deleteResource(corpus);
    Factory.deleteResource(mergerOne);
    controller.remove(mergerOne);
    controller.cleanup();
    Factory.deleteResource(controller);

    // finally {
    Gate.getUserConfig().put(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME,
      savedSpaceSetting);
    // }
  }

  private int obtainAnns(AnnotationSet anns, String f, String v) {
    int num = 0;
    for(Annotation ann : anns) {
      if(ann.getFeatures().containsKey(f) && ann.getFeatures().get(f).equals(v))
        ++num;
    }
    return num;
  }

}
