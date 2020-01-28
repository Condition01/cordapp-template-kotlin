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
) : FlowLogic<SignedTransaction>() {
    //When i change the Flowgic generic parameter a going to return a type of this generic in my
    //overrited function

    @Suspendable
    override fun call() : SignedTransaction{
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

        val txBuilder = TransactionBuilder(notary)
        //when you add an outputstate you also need to send the contract itself
        txBuilder.addOutputState(carState, CarContract.ID)
        txBuilder.addCommand(issueCommand)

        //You have to verify the transactions in this commandLine
        txBuilder.verify(serviceHub)

        //Here we can sign the initial transaction
        val tx = serviceHub.signInitialTransaction(txBuilder)

        //creating an flow for every node in the transaction excluding the sending node
        val sessions = (carState.participants - ourIdentity).map { initiateFlow(it as Party) }

        //create a subflow to collect the transactions signatures
        val stx = subFlow(CollectSignaturesFlow(tx, sessions))

        //the finalityFlow finalizes the transaction calling the notary or notary Pool
        return subFlow(FinalityFlow(stx, sessions))

    }
}

//Starts when the CarIssueIniatiorFlow is started
@InitiatedBy(CarIssueInitiator::class)
class CarIssueResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call() : SignedTransaction{
        // Responder flow logic goes here.
        // a object that implements the checkTransaction
        // verify the logic that we want in the response flow
        val signedTransactionFlow = object : SignTransactionFlow(counterpartySession){
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "The output must be a CarState" using (output is CarState)
            }
        }
        val txWeJustSignedId = subFlow(signedTransactionFlow)
        return subFlow(ReceiveFinalityFlow(counterpartySession, txWeJustSignedId.id))
    }
}
