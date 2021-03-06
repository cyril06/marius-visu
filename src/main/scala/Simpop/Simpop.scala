package fr.geocite.mariusvisu.Simpop

import fr.geocite.simpuzzle._
import fr.geocite.simpoplocal._
import scala.util.Random

import scala.collection.mutable.ListBuffer
import java.io.{File, FileWriter, BufferedWriter}

object Simpop extends App {

  import fr.geocite.simpuzzle._
  import fr.geocite.simpoplocal._

  val m = new StepByStep with SimpopLocalInitialState with SimpopLocalStep with SimpopLocalTimeInnovationEndingCondition  {
    def distanceDecay: Double = 0.6882107473716844
    def innovationImpact: Double = 0.007879556611500305
    def maxInnovation: Double = 10000
    def pCreation: Double = 1.2022185310640896E-6
    def pDiffusion: Double = 7.405303653131592E-7
    def rMax: Double = 10259.331894632433
  }

  val folderPath = "/tmp/"

  // PREMIER CSV

  val file = folderPath + "CreatedCumulatedByStep.csv"
  val writer = new BufferedWriter(new FileWriter(new File(file)))

  try{
    writer.append("created, diffused" + "\n")
  }

  //Premiere sortie innov
  for{ s <- m.states(new Random(-6863419716327549772L)) }{
    val diffusedByState = s.written.filter{
      case b: m.Diffused  => true
      case _ => false
    }.toSeq
    val createdByState = s.written.filter{
      case b: m.Created  => true
      case _ => false
    }.toSeq

    try {
      writer.append(createdByState.size + " , " + diffusedByState.size + "\n")
    }
  }
  writer.close()


  // DEUXIEME CSV
  val listInnov:List[(List[m.Created],List[m.Diffused])]= m.states(new Random(-6863419716327549772L)).map{ s =>
    println(s.value.step + " > " + s.value.currentInnovationId )
    (s.written.collect { case a:m.Created => a }.toList,
    s.written.collect { case a:m.Diffused => a }.toList)
    }.toList

  val listCreated = listInnov.map{ case (a,b) => a}
  val listDiffused = listInnov.map{ case (a,b) => b}

  println("size  = " + listCreated.flatten.size)
  val sizeListCreated = listCreated.flatten.groupBy{_.in}.mapValues(_.size)
  val sortedListCreated = sizeListCreated.toSeq.sortBy(_._1)

  println("size  = " + listDiffused.flatten.size)
  val sizeListDiffused = listDiffused.flatten.groupBy{_.from}.mapValues(_.size)
  val sortedListDiffused = sizeListDiffused.toSeq.sortBy(_._1)

  val file2 = folderPath + "CreatedCumulatedAllStep.csv"
  val writer2 = new BufferedWriter(new FileWriter(new File(file2)))

  try{
   writer2.append("id, created(byIn), diffused(byFrom)" + "\n")
  }

  (sortedListCreated zip sortedListDiffused).foreach{
    case ((id1,created),(id2,diffused)) =>
      try {
        writer2.append(id1 + " , " + created + " , " + diffused + "\n")
      }
  }

  writer2.close()

}
