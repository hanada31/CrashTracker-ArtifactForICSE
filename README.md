# [CrashTracker](https://github.com/hanada31/CrashTracker)

[TOC]

## Paper information

Artifact for ICSE 2023 paper：

> **Locating Framework-specific Crashing Faults with Compact and Explainable Candidate Set**

Abstract:
```
 Nowadays, many applications do not exist independently but rely on various frameworks or libraries. The frequent evolution and the complex implementation of APIs induce lots of unexpected post-release crashes. Starting from the crash stack traces, existing approaches either perform application-level call graph (CG) tracing or construct datasets with similar crash-fixing records to locate buggy methods. However, these approaches are limited by the completeness of CG or dependent on historical fixing records, and some of them only focus on specific manually modeled exception types. 
 
 To achieve effective debugging on complex framework-specific crashes, we propose a code-separation-based locating approach that weakly relies on CG tracing and does not require any prior knowledge. Our key insight is that one crash trace with the description message can be mapped to a definite exception-thrown point in the framework, the semantics analysis of which can help to figure out the root causes of the crash-triggering procedure. Thus, we can pre-construct reusable summaries for all the framework-specific exceptions to support fault localization in application code. Based on that idea, we design the exception-thrown summary (ETS) that describes both the key variables and key APIs related to the exception triggering.  Then, we perform static analysis to automatically compute such summaries and make a data-tracking of key variables and APIs in the application code to get the ranked buggy candidates. In the scenario of locating Android framework-specific crashing faults, our tool CrashTracker exhibited an overall MRR value of 0.91 and outperforms the state-of-the-art tool Anchor with higher precision. It only provides a compact candidate set and gives user-friendly reports with explainable reasons for each candidate.
```

Keywords: 

```
Fault localization, Framework-specific Exception, Crash Stack Trace, Android Application
```

Overview of Tool:

<p align="left">
<img src="Figures/LoFDroid-overview.png" width="70%">
</p>
------



## Contents for this artifact

- README.md. The ReadMe file for this artifact.

- [**CrashTrackerTool.**](CrashTrackerTool\)
  - [**README.md.** ](CrashTrackerTool\README.md) Instructions to use tool CrashTracker to achieve crash fault location for android framework-specific exceptions.
  - others. The source code and scripts of the tool **CrashTracker**.
  
- **EvaluationData.** The results that displayed in the evaluation section.
  - **Fault Localization Results.** Output of our tool CrashTracker using the default strategy on the dataset D580.
  
  - **Buggy Candidates Ranking.** The buggy candidates ranking results under multiple strategies.  
  
  - **Statistic.** Other statistic results that in the evaluation section.
  
- **CrashDataset**. A json format file, which is the dataset with 580 crash reports. Each report has its message,  exception type, crash trace, real buggy method label, etc.

- **FrameworkETS.** The constructed exception-thrown summaries for ten Android frameworks, which are a set of json format files. 

- **Figures**. Figures used in ReadMe file.

------



## Tips

For more information about the tool, please go to the CrashTracker\README.md file.

