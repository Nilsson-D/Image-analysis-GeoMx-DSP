import qupath.ext.biop.cellpose.Cellpose2D
import qupath.lib.analysis.features.ObjectMeasurements


//This script is inspired by https://forum.image.sc/t/combining-cellpose-and-stardist-detections-into-cells/78225

// Specify the model name 
pathModel_cyto = '<path to cytoplasm model>'

pathModel_nuc = '<path to nuclei model>'


param_pixelsize=.4//Pixel scale to perform segmentation at. Set to 0 for image resolution (default). Int values accepted, greater values  will be faster but may yield poorer segmentations.
param_tilesize=1024 //size of tile in pixels for processing. Must be a multiple of 16. Lower values may solve any memory-related errors, but can take longer to process. Default is 1024.
normalize_low_pct=0.0 //lower limit for normalization. Set to 0 to disable
normalize_high_pct=99.8 // upper limit for normalization. Set to 100 to disable.

def cellpose_nuc = Cellpose2D.builder( pathModel_nuc )
        .pixelSize(param_pixelsize)              // Resolution for detection
        .channels("Syto13")
        .tileSize(param_tilesize)   
        .diameter(0)                   // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .measureShape()                // Add shape measurements
        .measureIntensity()            // Add cell measurements (in all compartments)  
        .normalizePercentilesGlobal(normalize_low_pct, normalize_high_pct, 10)
        .setOverlap(200)
        .build()
        


def cellpose_cyto = Cellpose2D.builder( pathModel_cyto )
        .pixelSize(param_pixelsize)              // Resolution for detection
        .channels("CD3")
        .tileSize(param_tilesize)   
        .diameter(0)                   // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .measureShape()                // Add shape measurements
        .measureIntensity()            // Add cell measurements (in all compartments)  
        .normalizePercentilesGlobal(normalize_low_pct, normalize_high_pct, 10)
        .setOverlap(200)
        .build()
        
        
def cellpose_cyto2 = Cellpose2D.builder( pathModel_cyto )
        .pixelSize(param_pixelsize)              // Resolution for detection
        .channels("CD8")
        .tileSize(param_tilesize)   
        .diameter(0)                   // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .measureShape()                // Add shape measurements
        .measureIntensity()            // Add cell measurements (in all compartments)  
        .normalizePercentilesGlobal(normalize_low_pct, normalize_high_pct, 10)
        .setOverlap(200)
        .build()
        
        
def cellpose_cyto3 = Cellpose2D.builder( pathModel_cyto )
        .pixelSize(param_pixelsize)              // Resolution for detection
        .channels("CD57")
        .tileSize(param_tilesize)   
        .diameter(0)                   // Median object diameter. Set to 0.0 for the `bact_omni` model or for automatic computation
        .measureShape()                // Add shape measurements
        .measureIntensity()            // Add cell measurements (in all compartments)  
        .normalizePercentilesGlobal(normalize_low_pct, normalize_high_pct, 10)
        .setOverlap(200)
        .build()
        
// Run detection for the selected objects
imageData = getCurrentImageData()

tmalist = getTMACoreList()
//tmalist = getSelectedObjects()
for (core in tmalist) {
    
    //only segment valid cores
    if (core.isMissing() == false) {      
        selectObjects(core)
        pathObjects =  getSelectedObjects()
        all_core_children = pathObjects[0].getChildObjects()
        removeObjects(all_core_children, false)
        
        // Run detection for the selected pathObjects and store resulting detections
        cellpose_nuc.detectObjects(imageData, pathObjects)
        nucs = pathObjects[0].getChildObjects().collect()
        
        
        cellpose_cyto.detectObjects(imageData, pathObjects)
        cytos = pathObjects[0].getChildObjects().collect()
        
        cellpose_cyto2.detectObjects(imageData, pathObjects)
        cytos2 = pathObjects[0].getChildObjects().collect()
        
        cellpose_cyto3.detectObjects(imageData, pathObjects)
        cytos3 = pathObjects[0].getChildObjects().collect()    
        
        all_core_children = pathObjects[0].getChildObjects()
        removeObjects(all_core_children, false)    
        
        
        println "Done with cellpose segmentation. Starting computation of overlapping cells"
        
        // Combine cytos and nuclei detections to create cell objects by check that the nuclei center is inside the cell center
        // might change this to check the IoU instead
        
        
        cd3_cells = []
        cytos.each{ cyto ->
            nucs.each{ nuc ->      
                if ( cyto.getROI().contains( nuc.getROI().getCentroidX() , nuc.getROI().getCentroidY())){
                    //cells.add(PathObjects.createCellObject(cyto.getROI(), nuc.getROI(), getPathClass("CD3"), null ));
                    nuc.setPathClass(getPathClass("early non-cytotoxic")) //just change the class
                    cd3_cells.add(nuc)
                }
            }
        }
        
        dp_cd8_cells = []
        cytos2.each{ cyto2 ->
            cd3_cells.each{ cd3_cell ->      
                if ( cyto2.getROI().contains( cd3_cell.getROI().getCentroidX() , cd3_cell.getROI().getCentroidY())){
                    //cells.add(PathObjects.createCellObject(cyto.getROI(), nuc.getROI(), getPathClass("CD3"), null ));
                    cd3_cell.setPathClass(getPathClass("early cytotoxic"))
                    dp_cd8_cells.add(cd3_cell)
                }
            }
        }
        
        dp_cd57_cells = []
        cytos3.each{ cyto3 ->
            cd3_cells.each{ cd3_cell ->      
                if ( cyto3.getROI().contains( cd3_cell.getROI().getCentroidX() , cd3_cell.getROI().getCentroidY())){
                    //cells.add(PathObjects.createCellObject(cyto.getROI(), nuc.getROI(), getPathClass("CD3"), null ));
                    cd3_cell.setPathClass(getPathClass("late non-cytotoxic"))
                    dp_cd57_cells.add(cd3_cell)
                }
            }
        }    
        
        tp_cells = []
        cytos3.each{ cyto3 ->
            dp_cd8_cells.each{ dp_cd8_cell ->      
                if ( cyto3.getROI().contains( dp_cd8_cell.getROI().getCentroidX() , dp_cd8_cell.getROI().getCentroidY())){
                    //cells.add(PathObjects.createCellObject(cyto.getROI(), nuc.getROI(), getPathClass("CD3"), null ));
                    dp_cd8_cell.setPathClass(getPathClass("late cytotoxic"))
                    tp_cells.add(dp_cd8_cell)
                }
            }
        }        
        
        //add back all the cells again
        addObjects(nucs)
        addObjects(cd3_cells)
        addObjects(dp_cd8_cells)
        addObjects(dp_cd57_cells)
        addObjects(tp_cells)
   }
}



fireHierarchyUpdate()

println 'Done!'