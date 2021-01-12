1.12.2-1.15.2模板使用方法与说明

插件提供了一个模板用于1.12.2-1.15.2的同步模块编写

专有名称CapabilityProvider,Capability,CapabilityData

CapabilityProvider:
    继承自net.minecraftforge.common.capabilities.ICapabilityProvider,
    已知的子类有net.minecraftforge.common.capabilities.ICapabilitySerializable,实现了INBTSerializable接口
    
    对于实现了INBTSerializable接口的Provider,Forge才能序列化与反序列化mod模块数据

      一般在Forge事件AttachCapabilitiesEvent中,会有一个 特性 CapabilityProvider的注册过程
      模组会在该事件中通过调用AttachCapabilitiesEvent.register来添加CapabilityProvider
          参数1: CapabilityProvider的Key
          参数2: CapabilityProvider的实例


Capability:
    全类型为net.minecraftforge.common.capabilities.Capability<T>,T为数据类即CapabilityData或其接口
    其中Capability必须由MOD主动调用CapabilityManager.register注册
        参数1: CapabilityData实现的接口
        参数2: CapabilityData的序列化与反序列化方法(一般不用)
        参数3: CapabilityData实例提供者的类或实例
    经过注册后,MOD使用注解@CapabilityInject的字段才能接收到Forge实例化的Capability<T>


CapabilityData:
    CapabilityData为mod自定义类,无需继承任何接口
    CapabilityData与Capability一一对应

