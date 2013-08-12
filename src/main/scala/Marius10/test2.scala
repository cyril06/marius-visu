import fr.geocite.simpuzzle._
import fr.geocite.marius.one.zero._
import fr.geocite.marius._
import util.Random

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
}
