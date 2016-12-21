#Mon Feb 23 12:12:00 GMT 2015
return;=
fDefaultToolButton=createToolButton(IMAGES + "SEL", "Selection Tool", tool);
loadDrawing((String)e.getItem());=
createAttributeChoices(attributes);=
classpath=
JPanel=palette \= new JPanel();
getAppletContext().showDocument(url,="Help");
button=new JButton("Help");
fFillColor.setSelectedIndex(ColorMap.colorIndex(fillColor));=
getContentPane().add("Center",=(Component)view());
//=return the version of the package we are in
panel.add(button);=
jvm.mode=-1
choice.addItem(=
/*=
setTool(fDefaultToolButton.tool(),=fDefaultToolButton.name());
fSelectedToolButton.select();=
fillColor=(Color) f.getAttribute("FillColor");
startSleeper();=
getVersionControlStrategy().assertCompatibleVersion();=
javadocpath=http\://java.sun.com/j2se/1.5.0/docs/api;
fSelectedToolButton=button;
static=String                     fgUntitled \= "untitled";
project_dependencies=
fUpdateButton.setText("Buffered=Update");
attribute,=
showStatus(((ToolButton)=button).name());
fArrowChoice.setSelectedIndex(arrowMode.intValue());=
JButton=button;
showStatus("Unknown=file type");
fFontChoice.setSelectedItem(fontName);=
requiredVersions[0]=VersionManagement.getPackageVersion(DrawApplet.class.getPackage());
arrowMode=(Integer) f.getAttribute("ArrowMode");
panel.setLayout(new=PaletteLayout(2, new Point(2,2), false));
ignored_sourcepath=
initDrawing();=
fTool.deactivate();=
ColorMap.name(i),=
fTool.activate();=
fontName=(String) f.getAttribute("FontName");
if=(fSleeper \!\= null) {
createTools(toolPanel);=
textColor=(Color) f.getAttribute("TextColor");
param="";
stopSleeper();=
getContentPane().setLayout(new=BorderLayout());
ToolButton=toolButton \= (ToolButton) button;
StorableInput=input \= new StorableInput(stream);
JApplet=fApplet;
fDrawing=(Drawing)input.readObject();
FigureEnumeration=k \= view().selectionElements();
else={
fApplet.showStatus("loading=icons...");
fSelectedToolButton.reset();=
choice.addItem(new=ChangeAttributeCommand(fonts[i], "FontName", fonts[i],  this));
JComboBox=drawingChoice \= new JComboBox();
public=void run() {
palette.add(fDefaultToolButton);=
drawingChoice.addItem(st.nextToken());=
CommandChoice=choice \= new CommandChoice();
this=
createButtons(buttonPalette);=
setSelected(fDefaultToolButton);=
fSleeper.start();=
SleeperThread(JApplet=applet) {
try={
palette.setLayout(new=PaletteLayout(2,new Point(2,2)));
fUpdateButton=new JButton("Simple Update");
StringTokenizer=st \= new StringTokenizer(param);
extends=JApplet
autodetectpath=false
Tool=tool \= createSelectionTool();
fDrawing.release();=
loadAllImages(this);=// blocks until images loaded
drawingChoice.addItemListener(=
sourcepath=C\:\\Users\\Michael Mohan\\Documents\\PhD Work\\Code\\Others\\JHotDraw\\src\\CH\\ifa\\draw\\standard;
setUndoManager(new=UndoManager());
setSelected(toolButton);=
InputStream=stream \= url.openStream();
*/=
setTool(toolButton.tool(),=toolButton.name());
class=SleeperThread extends Thread {
new=ActionListener() {
while=(k.hasMoreElements()) {
showStatus("Class=not found\: " + e);
);=
panel.add(drawingChoice);=
import=CH.ifa.draw.util.*;
fSleeper=new SleeperThread(this);
/**=
showHelp();=
panel.add(fArrowChoice);=
fSimpleUpdate=false;
fFrameColor.setSelectedIndex(ColorMap.colorIndex(frameColor));=
//fTextColor.select(ColorMap.colorIndex(textColor));=
readFromStorableInput(filename);=
fFrameColor=createColorChoice("FrameColor");
ColorMap.color(i),=
fUpdateButton.addActionListener(=
fApplet=applet;
panel.add(new=Filler(6,20));
setBufferedDisplayUpdate();=
fTool=t;
setSimpleDisplayUpdate();=
protected=VersionControlStrategy getVersionControlStrategy() {
}=
implements=DrawingEditor, PaletteListener, VersionRequester {
view().setDrawing(fDrawing);=
drawingChoice.addItem(fgUntitled);=
button.addActionListener(=
String=appletPath \= getClass().getName().replace('.', '/');
Figure=f \= k.nextFigure();
toolDone();=
getContentPane().add("West",=toolPanel);
catch=(InterruptedException e) {
readDrawing(filename);=
fFillColor=createColorChoice("FillColor");
package=CH.ifa.draw.applet;
fFontChoice=createFontChoice();
for=(;;) {
fIconkit=new Iconkit(this);
private=void stopSleeper() {
panel.add(fFrameColor);=
readFromObjectInput(filename);=
ObjectInput=input \= new ObjectInputStream(stream);
//setBufferedDisplayUpdate();=
view().setDisplayUpdate(new=BufferedUpdateStrategy());
URL=url \= new URL(getCodeBase(), appletPath + "Help.html");
sleep(50);=
*=*** netscape browser work around ***
)=
Color=textColor  \= (Color)   AttributeFigure.getDefaultAttribute("TextColor");
panel.add(fFillColor);=
panel.add(fTextColor);=
setupAttributes();=
fSleeper.stop();=
fTextColor=createColorChoice("TextColor");
fView=createDrawingView();
showStatus("Error=" + e);
showStatus(name);=
String[]=requiredVersions \= new String[1];
getContentPane().add("North",=attributes);
frameColor=(Color) f.getAttribute("FrameColor");
fArrowChoice=choice;
/****=not sure whether this will still be needed on 1.1 enabled browsers
return=requiredVersions;
panel.add(fFontChoice);=
Integer=arrowMode  \= (Integer) AttributeFigure.getDefaultAttribute("ArrowMode");
myUndoManager=newUndoManager;
fUpdateButton.setText("Simple=Update");
showStatus("Help=file not found");
showStatus(fSelectedToolButton.name());=
getContentPane().add("South",=buttonPalette);
