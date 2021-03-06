package com.tuvistavie.analyzer;

import com.tuvistavie.analyzer.config.HashFrequencyAnalyzer;
import com.tuvistavie.analyzer.config.MapFrequencyAnalyzer;
import com.tuvistavie.analyzer.models.WordInfo;
import com.tuvistavie.analyzer.config.CLIParser;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class FrequencyRenderer extends Application {

  private int histogramRowsCount = 15;
  private String fileName;
  private boolean usesBuiltinHash = false;
  private List<WordInfo> data;
  private boolean onlyBenchmark = false;
  private int benchmarkTimes;

  public FrequencyRenderer() {
    super();
  }

  @Override
  public void init() throws Exception {
    super.init();
    initWithArgs();
    if (!initData()) {
      System.exit(0);
    }
  }

  @Override
  public void start(Stage stage) {
    stage.setTitle("Word occurrences in " + this.getFileName());
    final NumberAxis xAxis = new NumberAxis();
    final CategoryAxis yAxis = new CategoryAxis();
    final BarChart<Number,String> bc = new BarChart<>(xAxis,yAxis);
    bc.setTitle("Word occurrences in " + this.getFileName());
    xAxis.setLabel("Occurrences number");
    xAxis.setTickLabelRotation(90);
    yAxis.setLabel("Word");

    List<WordInfo> data = getData();
    XYChart.Series occurrenceSeries = new XYChart.Series();
    occurrenceSeries.setName("Word occurrences");

    @SuppressWarnings("unchecked") List<XYChart.Data<Number, String>> seriesData = occurrenceSeries.getData();

    for (int i = data.size() - 1; i >= 0; i--) {
      WordInfo wordInfo = data.get(i);
      seriesData.add(new XYChart.Data<>(wordInfo.occurrenceCount(), wordInfo.word()));
    }

    Scene scene  = new Scene(bc, 800, 600);

    bc.getData().add(occurrenceSeries);
    stage.setScene(scene);
    stage.show();
  }

  private List<WordInfo> analyzeFrequencies(InputStream is, int limit) {
    if (this.usesBuiltinHash()) {
      return MapFrequencyAnalyzer.getSortedFrequencies(is, limit);
    } else {
      return HashFrequencyAnalyzer.getSortedFrequencies(is, limit);
    }
  }


  private List<WordInfo> getData() {
    return this.data;
  }

  private void setData(List<WordInfo> data) {
    this.data = data;
  }

  private CommandLine getCli() {
    try {
      List<String> params = getParameters().getRaw();
      return CLIParser.parseArgs(params.toArray(new String[params.size()]));
    } catch (ParseException e) {
      CLIParser.printHelp();
      System.err.println(e.getLocalizedMessage());
      System.exit(1);
      return null;
    }
  }

  private boolean initData() {
    InputStream is = null;
    try {
      if (this.onlyBenchmark) {
        runBenchmark();
      } else {
        is = new FileInputStream(new File(this.getFileName()));
        this.setData(this.analyzeFrequencies(is, this.getHistogramRowsCount()));
        return true;
      }
    } catch (IOException e) {
      CLIParser.printHelp();
      System.exit(1);
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return false;
  }

  private void runBenchmark() throws IOException {
    for (int i = 0; i < this.getBenchmarkTimes(); i++) {
      InputStream is = new FileInputStream(new File(this.getFileName()));
      analyzeFrequencies(is, this.getHistogramRowsCount());
      is.close();
    }
  }

  private void initWithArgs() {
    CommandLine cli = getCli();
    this.setFileName(cli.getOptionValue("filename"));
    if (cli.hasOption("rows-count")) {
      this.setHistogramRowsCount(Integer.parseInt(cli.getOptionValue("rows-count")));
    }
    if (cli.hasOption("builtin-hash")) {
      this.setUsesBuiltinHash(true);
    }
    if (cli.hasOption("benchmark-times")) {
      this.onlyBenchmark = true;
      this.setBenchmarkTimes(Integer.parseInt(cli.getOptionValue("benchmark-times", "10")));
    }
  }

  public int getHistogramRowsCount() {
    return histogramRowsCount;
  }

  public void setHistogramRowsCount(int histogramRowsCount) {
    this.histogramRowsCount = histogramRowsCount;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public boolean usesBuiltinHash() {
    return usesBuiltinHash;
  }

  public void setUsesBuiltinHash(boolean usesBuiltinHash) {
    this.usesBuiltinHash = usesBuiltinHash;
  }

  public int getBenchmarkTimes() {
    return benchmarkTimes;
  }

  public void setBenchmarkTimes(int benchmarkTimes) {
    this.benchmarkTimes = benchmarkTimes;
  }
}


