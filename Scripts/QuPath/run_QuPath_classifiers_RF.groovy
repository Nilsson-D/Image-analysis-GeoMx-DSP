import static qupath.lib.gui.scripting.QPEx.*
import qupath.lib.objects.PathObjects
import qupath.lib.common.ColorTools
import qupath.lib.objects.classes.PathClass
import javafx.application.Platform
import qupath.lib.roi.GeometryROI
import qupath.lib.roi.GeometryTools

//Name of the current classifiers in this project (use insert and classifiers --> object classifiers to get the names)


def cd3_classifier = "cd3_classifier_all_images"
def cd8_classifier = "cd8_classifier_all_images"
def cd57_classifier = "cd57_classifier_all_images"


//update the classes to the cell types
//Platform.runLater {
//def pathClasses = getQuPath().getAvailablePathClasses()

//listOfClasses = [
//    getPathClass("early non-cytotoxic"),
//    getPathClass("early cytotoxic"),
//    getPathClass("late non-cytotoxic"),
//    getPathClass("late cytotoxic")
 //   ]

//pathClasses.setAll(listOfClasses)
//}


//run all the classifiers (sequentially applied)
runObjectClassifier(cd3_classifier, cd8_classifier, cd57_classifier)


//reset classification for cells positive for CD8-CD57, CD57 and CD8 alone
only_cd8_pos = getDetectionObjects().findAll {it.getPathClass() == getPathClass("CD8")}
only_cd57_pos = getDetectionObjects().findAll {it.getPathClass() == getPathClass("CD57")}
only_cd8_cd57_pos = getDetectionObjects().findAll {it.getPathClass() == getPathClass("CD8: CD57")}

//set the classification of each cell to null as these are not 
//interesting or are FPs (as defined)
for (cd8 in only_cd8_pos) {
    cd8.setPathClass(null)
}

for (cd57 in only_cd57_pos) {
    cd57.setPathClass(null)
}

for (cd8_cd57 in only_cd8_cd57_pos) {
    cd8_cd57.setPathClass(null)
}

//change the class name of each class to its corresponding type
pos = getDetectionObjects().findAll {it.getPathClass() == getPathClass("CD3: CD8")}
for (cell in pos) {
    cell.setPathClass(getPathClass('early cytotoxic'))
}

pos = getDetectionObjects().findAll {it.getPathClass() == getPathClass("CD3")}
for (cell in pos) {
    cell.setPathClass(getPathClass('early non-cytotoxic'))
}

pos = getDetectionObjects().findAll {it.getPathClass() == getPathClass("CD3: CD57")}
for (cell in pos) {
    cell.setPathClass(getPathClass('late non-cytotoxic'))
}

pos = getDetectionObjects().findAll {it.getPathClass() == getPathClass("CD3: CD8: CD57")}
for (cell in pos) {
    cell.setPathClass(getPathClass('late cytotoxic'))
}

println "Done!"