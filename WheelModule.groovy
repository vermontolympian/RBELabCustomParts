println "Loading 200x robot"
int depth=1
double gridUnits = 25
def wheelbaseIndex = 9
def wheelbaseIndexY = 9
def rideHeight = 75
def plateThickness =mm(1.0/4.0)
def wheelbase=gridUnits*wheelbaseIndex
CSGDatabase.clear()
LengthParameter printerOffset 			= new LengthParameter("printerOffset",0.5,[1.2,0])
String size ="M5"
HashMap<String, Object>  boltData = Vitamins.getConfiguration( "capScrew",size)
double nursertHeight = 9.5
double nutsertRad = 6.4/2+printerOffset.getMM()/2
double motorStandoffBoltLen = 45
double electronicsBayStandoff = 45

def nutsert=new Cylinder(nutsertRad,nursertHeight ).toCSG() 
def mm(def inches){
	return inches*25.4
}
def castor(){
	new Sphere(15.8/2)// Spheres radius
				.toCSG()
				.toZMax()
				.movez(20)
	.union([CSG.unionAll([new Cylinder(32/2,13).toCSG(),
		new Cylinder(5,13).toCSG().movex(38/2),
		new Cylinder(5,13).toCSG().movex(-38/2)
		]).hull(),
		new Cylinder(1.5,10).toCSG().toZMax().movex(38/2),
		new Cylinder(1.5,10).toCSG().toZMax().movex(-38/2)
		])
		.rotx(180)
}	
double tabThick = 3
double batteryShort = mm(2.64)
double batteryLong  =mm(5.27)
double centerline = gridUnits*wheelbaseIndex/2 
double leftX = centerline +gridUnits*1.5
double rightX = centerline -gridUnits*1.5
def leftNutsert = nutsert.toZMax()
			.movez(rideHeight)
			.movex( leftX)
def rightNutsert = nutsert.toZMax()
			.movez(rideHeight)
			.movex(rightX)
def rightNutsertSet= rightNutsert.movey(gridUnits)
for(double i=gridUnits*2;i<batteryLong;i+=gridUnits){
	//println "Right ="+i
	rightNutsertSet=rightNutsertSet.union(rightNutsert.movey(i))
}
def leftSet= leftNutsert.movey(gridUnits)
for(double i=gridUnits;i<batteryLong;i+=gridUnits){
	//println "Left ="+i
	leftSet=leftSet.union(leftNutsert.movey(i))
}

def batteryHoles= leftSet.union(rightNutsertSet)
def batteryBox = new Cube(batteryHoles.getTotalX()+tabThick*2,batteryLong+tabThick*2,batteryShort+tabThick).toCSG()
			.toYMin()
			.toZMax()
			.movez(rideHeight)
			.movex( centerline)
			.movey(-tabThick)
def battery = new Cube(batteryShort,batteryLong,batteryShort).toCSG()
			.toYMin()
			.toZMax()
			.movez(rideHeight)
			.movex( centerline)
def batteryBackHole = new Cube(batteryShort-tabThick*2,batteryLong+tabThick*2,batteryShort-tabThick).toCSG()
			.toYMin()
			.toZMax()
			.movez(rideHeight)
			.movex( centerline)		
def terminals = new Cube(batteryShort-15,30,25).toCSG()
			.toYMin()
			.toZMin()
			.movez(rideHeight)
			.movex( centerline)			
	//		.union([		leftNutsert,rightNutsert])
battery=battery.union(terminals)
batteryBox=batteryBox.difference([battery,
battery.movez(tabThick)
	  .movey(-tabThick),
	  batteryBackHole,
batteryHoles])
batteryBox.setName("batteryBox")
batteryBox.setManufacturing({ toMfg ->
	return toMfg
			.rotx(90)
			.toZMin()
})
CSG standoffCore = new Cylinder(nutsertRad*3,electronicsBayStandoff).toCSG()
CSG boltHole = new Cylinder(2.5+0.25,electronicsBayStandoff).toCSG()
double boltDepth = electronicsBayStandoff-(motorStandoffBoltLen -plateThickness-nursertHeight )
CSG boltHeadHole = new Cylinder(boltData.headDiameter/2.0+0.25,boltDepth).toCSG()
				.toZMax()
				.movez(electronicsBayStandoff)

def standoff = standoffCore.union(standoffCore.movex(gridUnits*3)).hull()
				.difference([
				nutsert,
				nutsert.movex(gridUnits*3),
				nutsert.toZMax().movez(electronicsBayStandoff),
				nutsert.toZMax().movez(electronicsBayStandoff).movex(gridUnits*3),
				boltHole.movex(gridUnits),
				boltHole.movex(gridUnits*2),
				boltHeadHole.movex(gridUnits),
				boltHeadHole.movex(gridUnits*2),
				])



def standoffLeft = standoff
				.movex(-gridUnits)
				.movez(rideHeight+ plateThickness)
def standoffRight = standoff
				.movex(gridUnits*7)
				.movez(rideHeight+ plateThickness)
double hingePoint = electronicsBayStandoff/2
double spacing = 0.5
double hingePartsThickness = (gridUnits-(spacing*2))/3
def hingeBolt = boltHole.roty(-90).movez(hingePoint)
CSG hingeCore = new Cylinder(nutsertRad*3,hingePartsThickness).toCSG()
def hingeBase =hingeCore.movex(gridUnits).union(hingeCore).hull()

CSG hingemount = new Cube(hingePartsThickness,nutsertRad*6,hingePartsThickness).toCSG()
				.toZMin()
				.toXMin()
CSG hingePillar = hingemount.union(hingeCore.roty(-90).movez(hingePoint)).hull()
def hingePillar2=hingePillar.movex(hingePartsThickness+spacing)
				.rotx(180)
				.movez(hingePoint*2)
def hingePillar3=hingePillar.movex((hingePartsThickness+spacing)*2)

def lower = hingeBase.union([hingePillar,hingePillar3])
			.difference([
			nutsert,
			nutsert.movex(gridUnits),
			nutsert.roty(-90).movez(hingePoint),
			hingeBolt
			])
def upper = hingeBase.toZMax().movez(electronicsBayStandoff).union([hingePillar2])
			.difference([
			nutsert.toZMax().movez(electronicsBayStandoff),
			nutsert.toZMax().movez(electronicsBayStandoff).movex(gridUnits),
			hingeBolt
			])
def hingeParts = [upper,lower]

def leftHinge = hingeParts.collect{
	it.move(gridUnits,gridUnits*5,rideHeight+ plateThickness)
}
def rightHinge = hingeParts.collect{
	it.move(gridUnits*7,gridUnits*5,rideHeight+ plateThickness)
}
def cableGuide = upper.move(gridUnits*4,gridUnits*5,rideHeight+ plateThickness)
//return [	battery,batteryBox,standoffLeft,standoffRight,leftHinge,rightHinge,cableGuide]





double washerThick = 1
def motorOptions = []
def shaftOptions = []
for(String vitaminsType: Vitamins.listVitaminTypes()){
	HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
	if(meta !=null && meta.containsKey("actuator"))
		motorOptions.add(vitaminsType);
	if(meta !=null && meta.containsKey("shaft"))
		shaftOptions.add(vitaminsType);
}

StringParameter motors = new StringParameter("Motor Type","roundMotor",motorOptions)
StringParameter shafts = new StringParameter("Shaft Type","dShaft",shaftOptions)
StringParameter motorSize = new StringParameter("Motor Size","WPI-gb37y3530-50en",Vitamins.listVitaminSizes(motors.getStrValue()))
StringParameter shaftSize = new StringParameter("Shaft Size","WPI-gb37y3530-50en",Vitamins.listVitaminSizes(shafts.getStrValue()))

def motorBlank= Vitamins.get(motors.getStrValue(),motorSize.getStrValue()).rotz(180)
def shaftBlank= Vitamins.get(shafts.getStrValue(),shaftSize.getStrValue())
HashMap<String, Object>  motorData = Vitamins.getConfiguration( motors.getStrValue(),motorSize.getStrValue())

double motorToMountPlane = motorBlank.getMaxZ()
double motorToMountPlaneMinusShoulder  = motorToMountPlane-motorData.shaftCollarHeight
double shaftLength=shaftBlank.getTotalZ()

motorBlank=motorBlank.movez(-motorToMountPlane)
double tireID = mm(1.0+(5.0/8.0))
double tireOD = mm(2.0)
double width = mm(3.0/16.0)
double axilToRideHeight = rideHeight-(tireOD/2)
double sweepCenter = (double)(tireOD+tireID)/4.0
def tire = CSG.unionAll(
		Extrude.revolve(new Cylinder(width/2-printerOffset.getMM()/2,1.0).toCSG().roty(90),
		sweepCenter, // rotation center radius, if 0 it is a circle, larger is a donut. Note it can be negative too
		(double)360,// degrees through wich it should sweep
		(int)30)//number of sweep increments
		)
		.roty(90)
def bearing =Vitamins.get("ballBearing","695zz").hull().makeKeepaway(printerOffset.getMM()).toZMin()
LengthParameter boltlen 			= new LengthParameter("Bolt Length",0.675,[1.2,0])
boltlen.setMM(50-1.388*2)

def bolt =new Cylinder(boltData.outerDiameter/2+printerOffset.getMM()/2,boltlen.getMM()).toCSG()



double pitch = 3
int atheeth =16
// call a script from another library
List<Object> bevelGears = (List<Object>)ScriptingEngine
					 .gitScriptRun(
            "https://github.com/madhephaestus/GearGenerator.git", // git location of the library
            "bevelGear.groovy" , // file to load
            // Parameters passed to the funcetion
            [	  atheeth,// Number of teeth gear a
	            atheeth*3,// Number of teeth gear b
	            6,// thickness of gear A
	            pitch,// gear pitch in arch length mm
	           90
            ]
            )
println "\tBevel gear radius A " + bevelGears.get(2)
println "\tBevel gear radius B " + bevelGears.get(3)
def gearBThickness = bevelGears.get(6)
def wheelSectionThickness = width*1.5
def wheelCenterlineX = -wheelSectionThickness/2-bevelGears.get(2)
def wheelCore = new Cylinder(sweepCenter,wheelSectionThickness).toCSG()
			.roty(-90)
			.movez(  bevelGears.get(3))
			.movex( -wheelSectionThickness-bevelGears.get(2))
def wheelWellCenter = new Cylinder(6,wheelSectionThickness+gearBThickness).toCSG()
			.roty(-90)
			.movez(  bevelGears.get(3))
			.movex( -wheelSectionThickness-bevelGears.get(2)-washerThick)
def wheelwell = new Cylinder(sweepCenter+width,wheelSectionThickness+gearBThickness).toCSG()
			.roty(-90)
			.movez(  bevelGears.get(3))
			.movex( -wheelSectionThickness-bevelGears.get(2)-washerThick)
			//.difference(wheelWellCenter)			
tire=tire .movez(  bevelGears.get(3))
		.movex( wheelCenterlineX)
bearing=bearing
		.roty(90)
		.movez(  bevelGears.get(3))
		.movex( gearBThickness-bevelGears.get(2)+washerThick)
bearing2 = bearing.movex(bearing.getTotalX() -gearBThickness-wheelSectionThickness-washerThick)
CSG outputGear = bevelGears.get(0)
CSG supportOutputGear = new Cylinder(outputGear.getTotalX()/2-0.1,Math.abs(motorToMountPlaneMinusShoulder)).toCSG()
					.toZMax()
outputGear=outputGear.union(	supportOutputGear)
CSG adrive = bevelGears.get(1)

def nutsertGrid= []
def netmover= nutsert
			.roty(90)

for(int i=-depth;i<=depth;i++)
	for(int j=-depth;j<=depth;j++){
		if(i==0&&j==0)
			continue
		nutsertGrid.add(netmover.movey(gridUnits*i)
				   .movez(gridUnits*j))
	}
def nutGrid = CSG.unionAll(nutsertGrid)
def axelBolt = bolt
			.movez(-boltlen.getMM()/2)
			.roty(-90)
			.movez(  bevelGears.get(3))
			.movex(motorBlank.getCenterX() )

def motorYold = sweepCenter+width+nursertHeight +Math.abs(motorBlank.getMinY())
def motorBracketYOffset = Math.abs(motorBlank.getMinY())
def motorY = axilToRideHeight+motorBracketYOffset+0.25
println "Motor height was "+motorYold+" and is "+motorY
def motorPlate = new Cube(boltlen.getMM(),motorY,motorToMountPlane).toCSG()
				.movex(motorBlank.getCenterX() )
				.toYMin()
				.movey(-motorBracketYOffset)
				.toZMax()
def axelMount = nutsert
			.roty(90)
			.movez(  bevelGears.get(3))
			.movex(motorPlate.getMaxX() )
def lSideGrid = nutGrid
			.movez(  bevelGears.get(3))
			.movex(motorPlate.getMaxX() )
def rSideGrid = nutGrid
			.movez(  bevelGears.get(3))
			.rotz(180)
			.movex(motorPlate.getMinX() )			
def leftHeight = motorPlate.getMaxX()-bevelGears.get(1).getMaxX()-printerOffset.getMM()//-washerThick
def rightHeight = Math.abs(motorPlate.getMinX()-wheelCore.getMinX())
def baseSupportRad = 17
def leftCone = new Cylinder(baseSupportRad, // Radius at the bottom
                      		boltData.outerDiameter/2+1, // Radius at the top
                      		leftHeight, // Height
                      		(int)30 //resolution
                      		).toCSG()
                      		.roty(90)
                      		.toXMax()
                      		.movex(motorPlate.getMaxX())
                      		.movez(  bevelGears.get(3))

println "Right cone height = "+rightHeight
def rightCone = new Cylinder(baseSupportRad, // Radius at the bottom
                      		boltData.outerDiameter/2+1, // Radius at the top
                      		rightHeight, // Height
                      		(int)30 //resolution
                      		).toCSG()
                      		.roty(-90)
                      		.toXMin()
                      		.movex(motorPlate.getMinX())
                      		.movez(  bevelGears.get(3))	
def  sideWallTHickness=motorPlate.getMaxX()-bevelGears.get(0).getMaxX()-5

def sideWallPuckL = new Cylinder(baseSupportRad, // Radius at the bottom
                      		baseSupportRad, // Radius at the top
                      		sideWallTHickness, // Height
                      		(int)30 //resolution
                      		).toCSG()
                      		.roty(90)
                      		.toXMax()
                      		.movex(motorPlate.getMaxX())
                      		.movez(  bevelGears.get(3))
 def sideWallPuckR = new Cylinder(baseSupportRad, // Radius at the bottom
                      		baseSupportRad, // Radius at the top
                      		sideWallTHickness, // Height
                      		(int)30 //resolution
                      		).toCSG()
                      		.roty(-90)
                      		.toXMin()
                      		.movex(motorPlate.getMinX())
                      		.movez(  bevelGears.get(3)) 
def mountWallBar =new Cube(sideWallTHickness,nursertHeight, bevelGears.get(3)+nutsertRad*2).toCSG()
				.toZMin()

def mountWallBarL = mountWallBar
				.toXMax()
				.movex(motorPlate.getMaxX())
				.toYMax()
				.movey(motorPlate.getMaxY())
def mountWallBarR=mountWallBar
				.toXMin()
				.movex(motorPlate.getMinX())
				.toYMax()
				.movey(motorPlate.getMaxY())
def sideWallBarL = new Cube(sideWallTHickness,motorY,motorToMountPlane).toCSG()
				.toXMax()
                    .movex(motorPlate.getMaxX())
				.toYMin()
				.movey(motorBlank.getMinY())
				.toZMax()
				.union([sideWallPuckL,mountWallBarL]).hull()
 def sideWallBarR = new Cube(sideWallTHickness,motorY,motorToMountPlane).toCSG()
				.toXMin()
                     .movex(motorPlate.getMinX())
				.toYMin()
				.movey(motorBlank.getMinY())
				.toZMax()
				.union([sideWallPuckR,mountWallBarR]).hull()  
def backPlate =  mountWallBarR.union(   mountWallBarL).hull()   

def wheelMountHole = nutsert
			.roty(90)
			.rotz(-90)
			.movez(  bevelGears.get(3))
			.movey(motorPlate.getMaxY() )
			.movex( wheelCenterlineX)
def wheelMountGrid = nutGrid
			.rotz(-90)
			.movez(  bevelGears.get(3))
			.movey(motorPlate.getMaxY() )
			.movex( wheelCenterlineX)		
def gearHole =  new Cylinder(bevelGears.get(0).getMaxX()+1,motorToMountPlane).toCSG() 
				.toZMax()     
println "Making bracket assembly"
// FInal assembly section				
def bracket = CSG.unionAll([motorPlate,leftCone,rightCone,sideWallBarL,
sideWallBarR,
backPlate
]).difference([wheelMountHole,wheelwell,gearHole])
.union(rightCone) 
.difference([axelBolt,motorBlank,
axelMount,
lSideGrid,rSideGrid,wheelMountGrid
])
println "Making wheel assembly"
def wheelAsmb = CSG.unionAll([adrive,wheelCore
]).difference([axelBolt,tire,bearing,bearing2

])
println "Making gear cutouts"
def driveGear = outputGear.difference([shaftBlank,motorBlank])
println "Making left side bracket"
def bracketm=bracket.mirrorx().movex(wheelbase+wheelCenterlineX*2)
// Attach production scripts
println "Alligning bracket to robot frame"
driveSection= [driveGear,bracket, bracketm,wheelAsmb,tire,motorBlank,bearing,bearing2].collect{
	it.move(-wheelCenterlineX,tire.getMaxY(),- bevelGears.get(3))
	.rotx(-90)
	}
println "Making castor"
def cast = castor()
double BottomOfPlate=driveSection[1].getMaxZ()
double castorStandoff = cast.getMinZ()
double standoffHeight = BottomOfPlate+castorStandoff
double castorDistanceY =gridUnits*(Math.round((wheelbaseIndexY/2.0))+1)
def standoffPart =CSG.unionAll([ 	new Cylinder(gridUnits/2,standoffHeight).toCSG().movex( gridUnits*0.5),
							new Cylinder(gridUnits/2,standoffHeight).toCSG().movex( -gridUnits*0.5)
				]).hull()
				.toZMax()
				.difference([	nutsert.toZMax().movex( gridUnits*0.5),
							nutsert.toZMax().movex( -gridUnits*0.5),
							
				])
				.movez(BottomOfPlate)
				.movex( gridUnits*wheelbaseIndex/2)
				.movey(castorDistanceY)
def movedCastor =cast.toZMin()
				.movex( gridUnits*wheelbaseIndex/2)
				.movey(castorDistanceY)
standoffPart=	standoffPart.difference(	movedCastor)	

println "Making hole grid"
def nutsertGridPlate= []
def netmoverP= new Cylinder(5.0/2,standoffHeight/2).toCSG()
			.toZMin()
			.movez(BottomOfPlate)
def netmoverV= new Cylinder(3/2,standoffHeight).toCSG()
			.toZMin()
			.movez(BottomOfPlate-10)
for(int i=0;i<wheelbaseIndexY+3;i++)
	for(int j=0;j<(wheelbaseIndex+3);j++){
		nutsertGridPlate.add(netmoverP.movey(gridUnits*i)
				   .movex(gridUnits*j-gridUnits))
}

for(int i=0;i<6;i++)
	for(int j=0;j<2;j++){
		nutsertGridPlate.add(netmoverV.movex(mm(0.5)*i+gridUnits/2)
				   .movey(mm(0.5)*j*7+gridUnits-gridUnits/2))
}
standoffPart=	standoffPart.difference(	movedCastor)	
			.difference(	nutsertGridPlate)	
wheelAsmb=driveSection[3]
println "Cutting grid from drive section"
bracketm=driveSection[2].difference(nutsertGridPlate)
bracket=driveSection[1].difference(nutsertGridPlate)
driveGear=driveSection[0]
tire = driveSection[4]
motorBlank = driveSection[5]
def driveGearl = driveGear.mirrorx().movex(wheelbase)
def wheelAsmbl = wheelAsmb.mirrorx().movex(wheelbase)
def tirel = tire.mirrorx().movex(wheelbase)
def motorBlankl=motorBlank.mirrorx().movex(wheelbase)
double plateRadius = ((gridUnits*(wheelbaseIndex+4))/2)+5
println "Plate Diameter "+(plateRadius*2.0/25.4)
CSG plateRound =new Cylinder(plateRadius,plateRadius,plateThickness,(int)60).toCSG() 
				.toZMin()
				.move(centerline,0,BottomOfPlate)
def plateCubic = new Cube(plateRadius*2,gridUnits*(wheelbaseIndexY+2),plateThickness).toCSG()
				.toZMin()
				.toYMin()
				.move(centerline,-gridUnits,BottomOfPlate)
def plate =  plateRound
				.intersect(plateCubic)
				.difference(nutsertGridPlate)
				.difference(battery)
def plate2 = plate .movez(electronicsBayStandoff+plateThickness)
println "Making mfg scripts "
wheelAsmb.setName("wheel")
	.setManufacturing({ toMfg ->
	return toMfg
			.roty(90)
			.toXMin()
			.toYMin()
			.toZMin()
})
wheelAsmbl.setName("wheel left")
	.setManufacturing({ toMfg ->
	return toMfg
			.roty(-90)
			.toXMin()
			.toYMin()
			.toZMin()
})
bracketm.setName("bracket-m")
	.setManufacturing({ toMfg ->
	return toMfg.rotx(90)
			.toXMin()
			.toYMin()
			.toZMin()
})
bracket.setName("bracket")
	.setManufacturing({ toMfg ->
	return toMfg.rotx(90)
			.toXMin()
			.toYMin()
			.toZMin()
})
driveGear.setName("driveGear")
	.setManufacturing({ toMfg ->
	return toMfg.rotx(90)
			.toXMin()
			.toYMin()
			.toZMin()
})
driveGearl.setName("driveGear left")
	.setManufacturing({ toMfg ->
	return toMfg.rotx(90)
			.toXMin()
			.toYMin()
			.toZMin()
})
standoffPart.setName("standoffPart")
	.setManufacturing({ toMfg ->
	return toMfg
			.toXMin()
			.toYMin()
			.toZMin()
})
standoffLeft.setName("standoffPartL")
	.setManufacturing({ toMfg ->
	return toMfg
			.toXMin()
			.toYMin()
			.toZMin()
})
standoffRight.setName("standoffPartR")
	.setManufacturing({ toMfg ->
	return toMfg
			.toXMin()
			.toYMin()
			.toZMin()
})
plate.addExportFormat("svg")// make an svg of the object
plate.setName("plate")
	.setManufacturing({ toMfg ->
	return toMfg
			.toZMin()
})
movedCastor.setManufacturing({ toMfg ->
	return null
})
tire.setName("tire")
tirel.setName("tirel")
tire.setManufacturing({ toMfg ->
	return null
})
tirel.setManufacturing({ toMfg ->
	return null
})
motorBlankl.setManufacturing({ toMfg ->
	return null
})
motorBlank.setManufacturing({ toMfg ->
	return null
})
battery.setManufacturing({ toMfg ->
	return null
})
plate2.setManufacturing({ toMfg ->
	return null
})
cableGuide.setName("cableGuide")
	.setManufacturing({ toMfg ->
	return toMfg
			.rotx(180)
			.toZMin()
})
leftHinge.get(0).setName("lHingeUpper")
	.setManufacturing({ toMfg ->
	return toMfg
			.rotx(180)
			.toZMin()
})
rightHinge.get(0).setName("rHingeUpper")
	.setManufacturing({ toMfg ->
	return toMfg
			.rotx(180)
			.toZMin()
})
leftHinge.get(1).setName("lHingeLower")
	.setManufacturing({ toMfg ->
	return toMfg
			.toZMin()
})
rightHinge.get(1).setName("rHingeLower")
	.setManufacturing({ toMfg ->
	return toMfg
			.toZMin()
})
println "BottomOfPlate = "+BottomOfPlate
println "Plate dimentions x="+plate.getTotalX()+" y="+plate.getTotalY()
println "Weel center line to outer wall of bracket="+Math.abs(bracket.getMinX())
parts=  [driveGear,driveGearl,bracket, bracketm,wheelAsmb,wheelAsmbl, movedCastor,standoffPart,
plate,tire,tirel,motorBlankl,
motorBlank,batteryBox,
standoffLeft,
standoffRight,
plate2,
leftHinge,rightHinge,
cableGuide
]
return parts

def toProject =[bracket,wheelAsmb,tire,plate]
def toProjectF =[bracket,bracketm,wheelAsmb,wheelAsmbl,tire,plate,tirel]
def toProjectb =[plate,movedCastor,standoffPart]
projection = toProject.collect{
	def back = it.roty(90)
				.movex(-gridUnits*3)
				.setName(it.getName()+"-projection")
	back.addExportFormat("svg")
	return back
}
projectionf = toProjectF.collect{
	def back = it.rotx(-90)
				.movey(-gridUnits*3)
				.setName(it.getName()+"-projection-f")
	back.addExportFormat("svg")
	return back
}
projectionb = toProjectb.collect{
	def back = it
	.movey(-gridUnits*(wheelbaseIndexY-1))
	.rotx(90)
	.movey(gridUnits*(wheelbaseIndexY-1))
				.movey(gridUnits*3)
				.setName(it.getName()+"-projection-b")
	back.addExportFormat("svg")
	return back
}
return [parts,projection,projectionf]
println "Making a single STL assembly..."
def singleSTL = CSG.unionAll(parts)
singleSTL.setName("fullAssembly_DO_NOT_PRINT")
singleSTL.setManufacturing({ toMfg ->
	return toMfg.movex(-wheelbase/2)
})
parts.add(singleSTL)
return parts