package com.template.states

import com.template.contracts.CarContract
//import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
// *********
// * State *
// *********
@BelongsToContract(CarContract::class)
data class CarState(
        var owningBank : Party,
        var holdingDealer : Party,
        var manufacturer : Party,
        var vin : String,
        var licensePlateNumber : String,
        var make : String,
        var model : String,
        var dealershipLocation : String,
        var linearId : UniqueIdentifier //id of the state
) : ContractState{
    override val participants: List<AbstractParty> =
            listOf(owningBank, holdingDealer, manufacturer)
}
