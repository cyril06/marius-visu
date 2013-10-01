/**
 * Created with IntelliJ IDEA.
 * User: paris
 * Date: 24/09/13
 * Time: 13:24
 * To change this template use File | Settings | File Templates.
 */

import fr.geocite.simpuzzle._
import fr.geocite.simpoplocal._
import scala.util.Random
import spray.json._
import DefaultJsonProtocol._
import java.io._
import math._



/*val m = new StepByStep with SimpopLocalInitialState with SimpopLocalStep with SimpopLocalTimeInnovationEndingCondition  {
  // Members declared in fr.geocite.simpoplocal.SimpopLocalInitialState
  def rMax: Double = 10586.4326224915

  // Members declared in fr.geocite.simpoplocal.SimpopLocalStep
  def distanceDecay: Double = 0.694803768487938
  def innovationImpact: Double = 0.00851454836373035
  def pDiffusion: Double = 1
  def pCreation: Double = 1

  // Members declared in fr.geocite.simpoplocal.SimpopLocalTimeInnovationEndingCondition
  def maxInnovation: Double = 10000
}*/

val myseed: Long = 6863419716327549772L
implicit val rng= new Random(myseed)

val m = new StepByStep with SimpopLocalInitialState with SimpopLocalStep with SimpopLocalTimeInnovationEndingCondition  {

  def distanceDecay: Double = 0.6882107473716844
  def innovationImpact: Double = 0.007879556611500305
  def maxInnovation: Double = 10000
  def pCreation: Double = 1.2022185310640896E-6
  def pDiffusion: Double = 7.405303653131592E-7
  def rMax: Double = 10259.331894632433
}


def genJSONReal()={
  val state=m.initial.value
  val long=state.settlements.map(_.x).toList
  val lat=state.settlements.map(_.y).toList
  val id=state.settlements.map(_.id).toList
  val popBegin=state.settlements.map(_.population).toList

  val data=(id.toList zip long zip lat zip popBegin).map{
    case (((o,lo),la),pop) => List(o,lo,la,pop)
  }

  val xyzMappedTable = data.map(v=>
  //(v(0),Map[String,String]("long"->v(1),"lat"->v(2),"pop2010"->v(3)))
    Map[String,String]("id"->v(0).toInt.toString,"long"->v(1).toString,"lat"->v(2).toString,"popBegin"->v(3).toString,"name"->v(0).toInt.toString)
  ).toIndexedSeq

  val writer=new PrintWriter(new File("/home/paris/simpopreal.js"))

  writer.write("var cities_def ="+xyzMappedTable.toJson.prettyPrint)

  writer.close()

}

def mean(l:Seq[Double]):Double = l.sum/l.length
def median(l:Seq[Double]):Double = if (l.length%2==1) l((l.length+1)/2) else mean(List(l(l.length/2),l(l.length/2+1)))
//Calcul des coefficients de la droite de regression
def variance(l:Seq[Double]):Double = {
  var moyenne=mean(l)
  l.map{case a=>pow(a-moyenne,2)}.sum/l.length
}
def ecartType(l:Seq[Double]):Double = sqrt(variance(l))
def covariance(l:Seq[Double],m:Seq[Double]):Double = {
  var moyL=mean(l)
  var moyM=mean(m)
  (l zip m).map{case (a,b)=>(a-moyL)*(b-moyM)}.sum/l.length
}
// Les rangs doivent etre dans l
def rPearson(l:Seq[Double],m:Seq[Double]):Double = covariance(l,m)/(ecartType(l)*ecartType(m))
def coeffRegress(l:Seq[Double],m:Seq[Double]):Tuple2[Double,Double] = {
  var coefDir=covariance(l,m)/variance(l)
  (coefDir,(mean(m)-coefDir*mean(l)))
}

//Calcul de la distance
def distance(l:Tuple2[Double,Double],m:Tuple2[Double,Double]): Double = sqrt(pow(l._1-m._1,2)+ pow(l._2-m._2,2) )


//Fichier contenant les donnees sur les villes
def getFlows(n:Int,text:String) = { //n est le nombre approximatif de steps -> un peu nul et text un text a faire apparaitre dans l'en tete comme le nom du modele

  val states=m.states

  //Construction de la liste contenant tous les couples pour tous les states
  var l:List[(Int,Int)]=List()
  var pop:Seq[Double]=List()
  var id:Seq[Int]=List()
  var ressources:Seq[Double]=List()
  //Nous conservons toutes les populations pour les pas de temps multiples de 100 ou de 10 (dependant du nb de steps) afin de pouvoir visualiser leur progression
  var popTotal:List[Seq[Double]]=List()
  var cpt:Int=0
  var mult:Int=if(n>=1000) 100 else 10

  while (states.hasNext) {
    val stateCurrent=states.next
    l=stateCurrent.written.map{case a:m.Diffused=>(a.from.toInt,a.to.toInt) case _ =>(0,0)}.filterNot{case(a,b)=>a==0 && b==0}.toList++l
    //On ne conserve que la derniere liste des populations
    pop= stateCurrent.value.settlements.map(_.population)
    id= stateCurrent.value.settlements.map(_.id)
    ressources= stateCurrent.value.settlements.map(_.availableResource)
    if (cpt%mult==0) {popTotal=popTotal++List(pop)}
    cpt=cpt+1
  }

  val writer=new PrintWriter(new File("/home/paris/simpopexchange.js"))

  val dataGenTableHeader=Map[String,String]("version"->text,"totalStep"->cpt.toString,"step"->cpt.toString,"oneStep"->"?","yearBegin"->"??","yearEnd"->"??")
  writer.write("var header="+dataGenTableHeader.toJson.prettyPrint)
  writer.write("\n")

  //(popTotal zipWithIndex).map{case(a,i)=>(i,(a zipWithIndex))}.map{case(i,a)=>(i,a.map{case(x,y)=>y->x}.toMap)}.map{case(i,a)=>i->a}.toMap

  //Calcul des données générales

  val idPopMappedTable = (id zip pop).map{case (a,b)=>a->b}.toMap
  val idResMappedTable = (id zip ressources).map{case (a,b)=>a->b}.toMap

  val exchangeMappedTable=l.groupBy(a=>a).mapValues(_.size).toIndexedSeq.map{case((a,b),c)=>(a.toString,b.toString,c)}.groupBy(_._1).toIndexedSeq.map{case (a,b)=>(a,idPopMappedTable(a.toInt),idResMappedTable(a.toInt),b)}

  //Nb d'innovation dont l'id est l'emetteur
  val innovOut=exchangeMappedTable.map(_._4).flatten.groupBy(_._1).mapValues(_.map(_._3).sum)
  //Nb de liens out
  val innovNbOut=exchangeMappedTable.map(_._4).flatten.groupBy(_._1).mapValues(_.size)

  //Nb d'innovation dont l'id est le recepteur
  val innovIn=exchangeMappedTable.map(_._4).flatten.groupBy(_._2).mapValues(_.map(_._3).sum)
  //Nb de liens in
  val innovNbIn=exchangeMappedTable.map(_._4).flatten.groupBy(_._2).mapValues(_.size)
  //Population initiale
  val popInitMap=(m.initial.value.settlements.map(_.population) zipWithIndex).map{case (a,b)=>b.toString->a}.toMap
  //Coordonnees pour calculer la distance
  val long=m.initial.value.settlements.map(_.x).toList
  val lat=m.initial.value.settlements.map(_.y).toList
  val coord=((long zip lat) zipWithIndex).map{case((a,b),c)=>c.toString->(a,b)}.toMap

 //On incorpore ces infos
  val exchangeAllInfo=exchangeMappedTable.map{case (a,b,c,d)=>(a,b,c,innovOut.getOrElse(a,0),innovIn.getOrElse(a,0),innovNbOut.getOrElse(a,0),innovNbIn.getOrElse(a,0),d)}

  val xyzMappedTable = exchangeAllInfo.map{case (from,pop,innov,out,in,nbOut,nbIn,vect)=>
    (Map[String,String]("orig"->from.toString,"pop"->pop.toString,"wealth"->innov.toString,"totalOut"->out.toString,"totalIn"->in.toString,"total"->(out-in).toString,"pop2010"->"none","nbFrom"->nbOut.toString,"nbTo"->nbIn.toString,"popInit"->popInitMap(from).toString))}

  val xyzMappedTableBis = exchangeAllInfo.map{case (from,pop,innov,out,in,nbOut,nbIn,vect)=>vect.map{case (a,b,c)=>
    (Map[String,String]("dest"->b.toString,"weight"->c.toString,"distance"->distance(coord(from),coord(b)).toString))} }

  writer.write("var cities_flows ="+(xyzMappedTable zip xyzMappedTableBis).toJson.prettyPrint)
  writer.write("\n")

  //Calculs sur la valeur des flux
  val notNullFlow=exchangeAllInfo.map{case (from,pop,innov,out,in,nbOut,nbIn,vect)=>vect.map{case (a,b,c)=>c}}.filterNot{case a=>a.isEmpty}.flatten.map(_.toDouble).toList
  val meanFlow=rint(mean(notNullFlow)*100)/100
  val minFlow=rint(notNullFlow.min*100)/100
  val maxFlow=rint(notNullFlow.max*100)/100
  val q1Flow=rint(notNullFlow(ceil(notNullFlow.length/4.0).toInt)*100)/100
  val q3Flow=rint(notNullFlow(ceil((notNullFlow.length/4.0)*3.0).toInt)*100)/100
  val medianFlow=rint(median(notNullFlow)*100)/100

  val dataGenTablePop=Map[String,Double]("mean"->mean(pop),"min"->pop.min,"max"->pop.max)
  // ressources = wealth
  val dataGenTableWealth=Map[String,Double]("mean"->rint(mean(ressources)*100)/100,"min"->rint(ressources.min*100)/100,"max"->rint(ressources.max*100)/100)
  val dataGenTableFlow=Map[String,Double]("nb"->l.length,"mean"->meanFlow,"min"->minFlow,"max"->maxFlow,"q1"->q1Flow,"q3"->q3Flow,"median"->medianFlow)
  val dataGenTableExchange=Map[String,Double]("nbmax"->(innovOut.values++ innovIn.values).max,"maxSumIn"->innovIn.values.max,"maxSumOut"->innovOut.values.max)

  // Carte commercial
  val total=exchangeAllInfo.map{case(from,pop,innov,out,in,nbOut,nbIn,vect)=>(out-in)}.filterNot{case a=>a==0}.partition(a=>a>0)
  val totalPos=List(total).map{case (a,b)=>a}.flatten.map(_.toDouble)
  val totalPosMean=rint(mean(totalPos)*100)/100
  val totalPosMax=rint(totalPos.max*100)/100
  val totalPosMin=rint(totalPos.min*100)/100
  val totalPosNb=totalPos.length
  val totalNeg=List(total).map{case (a,b)=>b}.flatten.map(_.toDouble)
  val totalNegMean=rint(mean(totalNeg)*100)/100
  val totalNegMax=rint(totalNeg.max*100)/100
  val totalNegMin=rint(totalNeg.min*100)/100
  val totalNegNb=totalNeg.length
  val dataGenTableTotal=Map[String,Double]("nbPos"->totalPosNb,"meanPos"->totalPosMean,"minPos"->totalPosMin,"maxPos"->totalPosMax,"nbNeg"->totalNegNb,"meanNeg"->totalNegMean,"minNeg"->totalNegMin,"maxNeg"->totalNegMax)

  val dataGenTable=Map[String,Map[String,Double]]("pop"->dataGenTablePop,"wealth"->dataGenTableWealth,"flow"->dataGenTableFlow,"exchange"->dataGenTableExchange,"commercial"->dataGenTableTotal)
  writer.write("var dataGen="+dataGenTable.toJson.prettyPrint)


  //Boites a moustaches
  val popCitiesPos=exchangeAllInfo.map{case(from,pop,innov,out,in,nbOut,nbIn,vect)=>(pop,out-in)}.filter{case (a,b)=>b>0}.map{case(a,b)=>a}.sortWith(_<_)
  val popCitiesNeg=exchangeAllInfo.map{case(from,pop,innov,out,in,nbOut,nbIn,vect)=>(pop,nbOut-nbIn)}.filter{case (a,b)=>b<0}.map{case(a,b)=>a}.sortWith(_<_)
  val meanCitiesPos=rint(mean(popCitiesPos)*100)/100
  val meanCitiesNeg=rint(mean(popCitiesNeg)*100)/100
  val q1Pos=rint(popCitiesPos(ceil(popCitiesPos.length/4.0).toInt)*100)/100
  val q3Pos=rint(popCitiesPos(ceil((popCitiesPos.length/4.0)*3.0).toInt)*100)/100
  val medianPos=rint(median(popCitiesPos)*100)/100
  val q1Neg=rint(popCitiesNeg(ceil(popCitiesNeg.length/4.0).toInt)*100)/100
  val q3Neg=rint(popCitiesNeg(ceil((popCitiesNeg.length/4.0)*3.0).toInt)*100)/100
  val medianNeg=rint(median(popCitiesNeg)*100)/100

  val dataGenTablePos=Map[String,Double]("min"->rint(popCitiesPos.min*100)/100,"max"->rint(popCitiesPos.max*100)/100,"q1"->q1Pos,"q3"->q3Pos,"median"->medianPos,"mean"->meanCitiesPos)
  val dataGenTableNeg=Map[String,Double]("min"->rint(popCitiesNeg.min*100)/100,"max"->rint(popCitiesNeg.max*100)/100,"q1"->q1Neg,"q3"->q3Neg,"median"->medianNeg,"mean"->meanCitiesNeg)

  val dataGenTableWhiskers=Map[String,Map[String,Double]]("positive"->dataGenTablePos,"negative"->dataGenTableNeg)
  writer.write("\n")
  writer.write("var whiskers="+dataGenTableWhiskers.toJson.prettyPrint)


  writer.close()

  //Generation d'un fichier contenant tout l'historique precedant le step n (ici tous les steps)
  //popTotal contient toutes les populations par pas de temps
  //Creation d'une variable du type List((pas de temps,Vector((pop,id de la ville))))
  val intermedPop=(popTotal zipWithIndex).map{case(a,i)=>(i,a.zipWithIndex)}
  //Regroupement des villes par leur id List((id,List((pas de temps,pop,id))))
  val allPop=intermedPop.map{case (i,a)=>a.map{case (x,y)=>(i,x,y.toString)}}.flatten.groupBy(_._3).toList

  //Ecriture du resultat dans un fichier
  val writerBis=new PrintWriter(new File("/home/paris/simpopallpop.js"))

  val xyzMappedTable2 = allPop.map{case (a,b)=>
    (Map[String,String]("id"->a))}
  val xyzMappedTable2Bis = allPop.map{case (a,b)=>b.map{case (x,y,z)=>
    (Map[String,String]("step"->(x*mult).toString,"pop"->(rint(y*100)/100).toString))}}

  writerBis.write("var cities_pop ="+(xyzMappedTable2 zip xyzMappedTable2Bis).toJson.prettyPrint)
  writerBis.write("\n")

  //Calcul des coefficients des doites de regression

  //Les populations de villes à tous les pas de temps sont contenus dans la variable intermedPop. On les trie et on applique le logarithme
  var intermedPopTrie=intermedPop.map{case (a,b)=>b.map{case (x,y)=>x}}.map(_.sortWith(_>_)).map(_.toList).map(_.map(x=>log(x)))
  // On applique la fonction coeffRegress avec comme liste l les logarithmes des rangs et m intermedPopTrie
  var listCoeff=intermedPopTrie.map(a=>coeffRegress(List(1 to a.length).flatten.map(_.toDouble).map(x=>log(x)),a))

  //On écrit le resultat dans le fichier
  val xyzMappedTable3 = ((listCoeff zip popTotal.map(_.sum)) zipWithIndex).map{case (((a,b),c),d)=>
    (Map[String,String]("step"->(d*100).toString,"coeff"->a.toString,"popTotal"->(rint(c*100)/100).toString))}
  writerBis.write("var cities_coeff ="+(xyzMappedTable3).toJson.prettyPrint)

  writerBis.close()

}

getFlows(1000,"simpoplocal")
//genJSONReal()