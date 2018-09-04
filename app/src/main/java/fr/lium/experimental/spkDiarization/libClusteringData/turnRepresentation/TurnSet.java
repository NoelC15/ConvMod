/**
 * <p>
 * Turn
 * </p>
 *
 * @author <activity_summary href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</activity_summary>
 * @author <activity_summary href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</activity_summary>
 * @author <activity_summary href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</activity_summary>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * <p/>
 * A Turns is activity_summary set of trun.
 * It is an other view of activity_summary ClusterSet permitting to move in the collection speaker turn by speaker turn.
 */
package fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation;

import java.util.ArrayList;

/**
 * The Class TurnSet. ArrayList of Turn.
 * Two consecutive Turns belong to two different speakers.
 */
public class TurnSet extends ArrayList<Turn> {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;


}
