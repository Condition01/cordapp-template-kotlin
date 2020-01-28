package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.CarContract
import com.template.states.CarState
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

// *********
// * Flows *
// *********
@InitiatingFlow
@StartableByRPC
class CarIssueInitiator(
        var owningBank : Party,
        var holdingDealer : Party,
        var manufacturer : Party,
        var vin : String,
        var licensePlateNumber : String,
        var make : String,
        var model : String,
        var dealershipLocation : String
) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        // Initiator flow logic goes here.
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // This command will say wich type of command i'm going to use and
        // who's going to sign the transaction
        // The command object receive a CommandData and a List of Signers
        val issueCommand = Command(CarContract.Commands.Issue(), listOf(owningBank
                ,holdingDealer,manufacturer).map { it.owningKey })

        val carState = CarState(owningBank,holdingDealer,manufacturer,vin,
                        licensePlateNumber, make, model, dealershipLocation,
                        UniqueIdentifier())

    }
}

@InitiatedBy(CarIssueInitiator::class)
class CarIssueResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // Responder flow logic goes here.
    }
}
