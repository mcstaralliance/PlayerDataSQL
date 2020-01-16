1.12.2模板使用规则

插件提供了一个模板用于1.12.2的同步模块编写
以下统称Capability为特性


CapabilityProvider型,特性提供器

    需求:
        需要继承自net.minecraftforge.common.capabilities.ICapabilitySerializable,或
        继承net.minecraftforge.common.capabilities.ICapabilityProvider,其中,前者继承自后者

    判断依据:
        一般在Forge事件AttachCapabilitiesEvent中,会有一个 特性 CapabilityProvider的添加过程
        模组会在该事件中通过调用AttachCapabilitiesEvent.addCapability来添加 特性提供器
            参数1: 特性实现的接口
            参数2: 特性的保存类实例
            参数3: 特性实例提供者的类或实例


    ＣapabilityProvider的序列化:
          接口net.minecraftforge.common.capabilities.ICapabilityProvider默认未实现NBT序列化接口INBTSerializable,
          接口net.minecraftforge.common.capabilities.ICapabilitySerializable已经实现接口INBTSerializable
          对于已经实现INBTSerializable的提供器,模板都能自动序列化数据,但是对于未实现的模组,需要自己重写序列化与
          反序列化方法或者替换相应的方法



关于Capability<T>
特性由模组主动注册产生,注册方法为CapabilityManager.register
在此注册过程中,同时为特性实例提供了序列化方法与特性生成方法

关于ICapabilitySerializable
此为特性的统一化序列化方法,默认调用CapabilityManager.register中注册的序列化方法
