package com.example.wisechoice

class TempBlock {
    private var Amount: String = ""
    private var Block_No: String = ""
    private var Fees: String = ""
    private var Receiver: String = ""
    private var Sender: String = ""
    private var Temp_Block: String = ""
    private var Transaction_Time: String = ""
    private var Verify: String = ""

    fun setSender(sender: String) {
        this.Sender = sender
    }

    fun setReceiver(receiver: String) {
        this.Receiver = receiver
    }

    fun setAmount(amount: String) {
        this.Amount = amount
    }

    fun setFees(fees: String) {
        this.Fees = fees
    }

    fun setVerify(verify: String) {
        this.Verify = verify
    }

    fun setTemp_Block(tempBlock: String) {
        this.Temp_Block = tempBlock
    }

    fun setBlock_No(blockNo: String) {
        this.Block_No = blockNo
    }

    fun setTransaction_Time(transactionTime: String) {
        this.Transaction_Time = transactionTime
    }

    // Getter methods
    fun getSender(): String {
        return Sender
    }

    fun getReceiver(): String {
        return Receiver
    }

    fun getAmount(): String {
        return Amount
    }

    fun getFees(): String {
        return Fees
    }

    fun getVerify(): String {
        return Verify
    }

    fun getTemp_Block(): String {
        return Temp_Block
    }

    fun getBlock_No(): String {
        return Block_No
    }

    fun getTransaction_Time(): String {
        return Transaction_Time
    }
}