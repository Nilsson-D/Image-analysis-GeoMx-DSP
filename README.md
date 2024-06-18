# Image-analysis-GeoMx-DSP

## Description

This repo contains information and code used for analysing images from the GeoMx digital spatial profiler for the T-cell paper (add link). The images were stained with 4 markers.

## Software and libraries
The image analysis was performed with the open source software QuPath. 

The nuclei segmentation was performed with a fine-tuned "nuclei" Cellpose model using the Cellpose GUI. The "cyto" model was fine-tuned for the cytoplasm segmentation.

## Data analysis workflow
For running the QuPath classification:
1. run_cellpose.groovy runs the nuclei model with cell expansion
2. run_QuPath_classifiers_RF.groovy classifies the cells to their cell types

For running the Cellpose classification
1. classify_cells_by_cellpose_segmentation.groovy runs segmentation and overlap checking with classification

For exporting images and labeled images with class maps:
1. export_labeled_images_with_classMap.groovy
The output is later used in calculate_metrics.ipynb to calculate the metrics used for evaluation of the different methods. 
