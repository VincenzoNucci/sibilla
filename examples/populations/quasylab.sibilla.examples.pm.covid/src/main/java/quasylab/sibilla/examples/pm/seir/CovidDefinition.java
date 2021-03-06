/*
 * Sibilla:  a Java framework designed to support analysis of Collective
 * Adaptive Systems.
 *
 * Copyright (C) 2020.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package quasylab.sibilla.examples.pm.seir;

import quasylab.sibilla.core.models.MeasureFunction;
import quasylab.sibilla.core.models.Model;
import quasylab.sibilla.core.models.ModelDefinition;
import quasylab.sibilla.core.models.StatePredicate;
import quasylab.sibilla.core.models.pm.*;
import quasylab.sibilla.core.simulator.sampling.StatisticSampling;

public class CovidDefinition implements ModelDefinition<PopulationState> {

    public final static int S = 0;
    public final static int A = 1;
    public final static int G = 2;
    public final static int R = 3;
    public final static int D = 4;

    public final static int SCALE = 100;
    public final static int INIT_S = 99*SCALE;
    public final static int INIT_A = 1*SCALE;
    public final static int INIT_G = 0*SCALE;
    public final static int INIT_R = 0*SCALE;
    public final static int INIT_D = 0*SCALE;
    public final static double N = INIT_S + INIT_A + INIT_G + INIT_R + INIT_D;

    public final static double LAMBDA_MEET = 4;
    public final static double PROB_TRANSMISSION = 0.1;
    public final static double LAMBDA_R_A = 1 / 7.0;
    public final static double LAMBDA_R_G = 1 / 15.0;

    private final static double PROB_ASINT = 0.8;
    private final static double PROB_A_G = 0.5;
    private final static double PROB_DEATH = 0.02;


    @Override
    public int stateArity() {
        return 0;
    }

    @Override
    public int modelArity() {
        return 0;
    }

    @Override
    public PopulationState state(double... parameters) {
        if (parameters.length != 5) {
            return new PopulationState( new int[] { INIT_S, INIT_A, INIT_G, INIT_R, INIT_D } );
        } else {
            return new PopulationState( new int[] {
                    (int) parameters[S],
                    (int) parameters[A],
                    (int) parameters[G],
                    (int) parameters[R],
                    (int) parameters[D] });
        }
    }

    @Override
    public Model<PopulationState> createModel(double... args) {
        double lambda = (args.length>0?args[0]:LAMBDA_MEET);
        PopulationRule rule_S_A_A = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(A)} ,
                new Population[] { new Population(A), new Population(A)},
                (t,s) -> PROB_ASINT*s.getOccupancy(S)* PROB_TRANSMISSION*lambda *(s.getOccupancy(A)/N));

        PopulationRule rule_S_G_A = new ReactionRule(
                "S->G",
                new Population[] { new Population(S), new Population(A)} ,
                new Population[] { new Population(G), new Population(A)},
                (t,s) -> (1-PROB_ASINT)*s.getOccupancy(S)* PROB_TRANSMISSION*lambda *(s.getOccupancy(A)/N));

        PopulationRule rule_S_A_G = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(G)} ,
                new Population[] { new Population(A), new Population(G)},
                (t,s) -> PROB_ASINT*s.getOccupancy(S)* PROB_TRANSMISSION*lambda *(s.getOccupancy(G)/N));

        PopulationRule rule_S_G_G = new ReactionRule(
                "S->A",
                new Population[] { new Population(S), new Population(G)} ,
                new Population[] { new Population(G), new Population(G)},
                (t,s) -> (1-PROB_ASINT)*s.getOccupancy(S)* PROB_TRANSMISSION*lambda *(s.getOccupancy(G)/N));

        PopulationRule rule_A_R = new ReactionRule(
                "I->R",
                new Population[] { new Population(A) },
                new Population[] { new Population(R) },
                (t,s) -> s.getOccupancy(A)*LAMBDA_R_A*(1-PROB_A_G)
        );

        PopulationRule rule_A_G = new ReactionRule(
                "I->R",
                new Population[] { new Population(A) },
                new Population[] { new Population(G) },
                (t,s) -> s.getOccupancy(A)*LAMBDA_R_A*PROB_A_G
        );

        PopulationRule rule_G_R = new ReactionRule(
                "I->R",
                new Population[] { new Population(G) },
                new Population[] { new Population(R) },
                (t,s) -> s.getOccupancy(G)*LAMBDA_R_G*(1-PROB_DEATH)
        );

        PopulationRule rule_G_D = new ReactionRule(
                "I->R",
                new Population[] { new Population(G) },
                new Population[] { new Population(D) },
                (t,s) -> s.getOccupancy(G)*LAMBDA_R_G*PROB_DEATH
        );

        PopulationModel f = new PopulationModel();
        f.addRule(rule_S_A_A);
        f.addRule(rule_S_G_A);
        f.addRule(rule_S_A_G);
        f.addRule(rule_S_G_G);
        f.addRule(rule_A_G);
        f.addRule(rule_A_R);
        f.addRule(rule_G_R);
        f.addRule(rule_G_D);
        return f;
    }


}
