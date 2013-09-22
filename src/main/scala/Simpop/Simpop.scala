package fr.geocite.mariusvisu.Simpop

import fr.geocite.simpuzzle._
import fr.geocite.simpoplocal._

import rng._

object Simpop extends App {

  import fr.geocite.simpuzzle._
  import fr.geocite.simpoplocal._
  import rng._

  val m = new StepByStep with SimpopLocalInitialState with SimpopLocalStep with SimpopLocalTimeInnovationEndingCondition  {
    // Members declared in fr.geocite.simpoplocal.SimpopLocalInitialState
    def rMax: Double = 10586.4326224915

    // Members declared in fr.geocite.simpoplocal.SimpopLocalStep
    def distanceDecay: Double = 0.694803768487938
    def innovationImpact: Double = 0.00851454836373035
    def pDiffusion: Double = 1
    def pCreation: Double = 1

    // Members declared in fr.geocite.simpoplocal.SimpopLocalTimeInnovationEndingCondition
    def maxInnovation: Double = 10000
  }

  for{ s <- m.states.take(100) }{
    println(s.written)
    println((s.value.settlements.map(_.population).sum))
  }


}

