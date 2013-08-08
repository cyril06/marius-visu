/*import fr.geocite.marius._
import fr.geocite.marius.one.zero._
import fr.geocite.simpuzzle._
import spray.json._
import org.apache.commons.math3.stat.StatUtils

import scala.util.Random

case class XY_JSON(key: String, values: Seq[Map[String, Double]])

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val XYFormat = jsonFormat2(XY_JSON)
}

object JSONConverter {
  def main(args: Array[String]) = {

    import rng._

    val m = new StepByStep with MariusInitialState with MariusStep with TimeEndingCondition with MariusFile {
      def hydrocarbonBonus: Double = 0.01

      def rate: Double = 0.01

      def stdRate: Double = 0.005

      def maxStep: Int = 100
    }


    println(m.states.toList.last.cities.map(_.population).sorted.reverse)

    println(StatUtils.mean(Array(5.0,8.0,9.0,8.0,4.0,5.0,2.0,4.0)))

    val XYZTable = List(Array(1, 1.8),Array(2, 8.8),Array(3, 1.0))

    val xyzMappedTable = XYZTable.map { v ⇒
      Map[String, Double]("id" -> v(0), "pop2010" -> v(1))
    }.toIndexedSeq

    val cities = List("1")

    import MyJsonProtocol._

    val JsonData = cities.map{ case (ci) => XY_JSON(ci, xyzMappedTable)}.toJson

    println(JsonData.toString())

  }
}

object Test extends App {

  import rng._

  val m = new StepByStep with MariusInitialState with MariusStep with TimeEndingCondition with MariusFile {
    def hydrocarbonBonus: Double = 0.01

    def rate: Double = 0.01

    def stdRate: Double = 0.005

    def maxStep: Int = 100
  }


  println(m.states.toList.last.cities.map(_.population).sorted.reverse)
}*/

import fr.geocite.simpuzzle._
import fr.geocite.marius.one.zero._
import fr.geocite.marius._
import util.Random

object Test extends App {
implicit val rng = new Random(42)

val marius = new StepByStep with MariusInitialState with MariusStep with TimeEndingCondition with MariusFile with PowerInitialWealth {
// Members declared in fr.geocite.marius.one.zero.MariusStep
def adjustConsumption: Double = 0.6
def adjustProductivity: Double = 0.3
def capitalShareOfTaxes: Double = 0.0
def conversionFactor: Double = 50.0
def distanceDecay: Double = 1.0
def distanceOrderBuy: Double = 0.6
def distanceOrderSell: Double = 0.4
def partnerMultiplier: Double = 8.0
def territorialTaxes: Double = 0.4

// Members declared in fr.geocite.marius.one.zero.PowerInitialWealth
def wealthExponent: Double = 1.1

// Members declared in fr.geocite.simpuzzle.TimeEndingCondition
def maxStep: Int = 51
}

marius.states.foreach(s => println(s.step))

import scalax.io._
val output:Output = Resource.fromFile("/tmp/marius.csv")

marius.states.foreach {
s => println(s.step)
s.cities.zipWithIndex.foreach{
case(c, id) => output.write(s"${s.step},$id,${c.population},${c.wealth},${c.region},${c.capital},27.0\n")
}
}}

