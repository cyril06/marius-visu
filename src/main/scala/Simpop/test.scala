/**
 * Created with IntelliJ IDEA.
 * User: Cyril
 * Date: 16/09/13
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */

import fr.geocite.simpuzzle._
import fr.geocite.simpoplocal._

import rng._

val m = new StepByStep with SimpopLocalInitialState with SimpopLocalStep with SimpopLocalTimeInnovationEndingCondition  {
  // Members declared in fr.geocite.simpoplocal.SimpopLocalInitialState
  def rMax: Double = 10586.4326224915

  // Members declared in fr.geocite.simpoplocal.SimpopLocalStep
  def distanceDecay: Double = 0.694803768487938
  def innovationImpact: Double = 0.00851454836373035
  def pDiffusion: Double = 8.67248270179271E-07
  def pCreation: Double = 8.67248270179271E-07

  // Members declared in fr.geocite.simpoplocal.SimpopLocalTimeInnovationEndingCondition
  def maxInnovation: Double = 10000
}

val begin = System.currentTimeMillis
(0 until 10).map(_ => m.run.cities.map(_.population).sum).sum / 10
println(System.currentTimeMillis - begin)
