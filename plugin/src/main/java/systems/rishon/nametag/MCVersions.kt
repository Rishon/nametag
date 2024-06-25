package systems.rishon.common

import systems.rishon.v1_21.V1_20_6
import systems.rishon.v1_21.V1_21

object MCVersions {

    fun getV1_20_6(): IPacket {
        return V1_20_6()
    }

    fun getV1_21(): IPacket {
        return V1_21()
    }

}